import asyncio
import json
import logging
import websockets
from datetime import datetime
from PyQt5.QtWidgets import QMessageBox
from .file_sync_manager import FileSyncManager
from .database_operations import DatabaseOperations

class SocketClient:
    def __init__(self, config_path, tokenManager, fileSyncManager, app):
        self.config = self.load_config(config_path)
        self.host = self.config['server_url']
        self.port = self.config['socket_port']
        self.tokenManager = tokenManager
        self.fileSyncManager = fileSyncManager
        self.app = app
        self.websocket = None
        self.isRunning = False

    def load_config(self, config_path):
        try:
            with open(config_path, 'r') as config_file:
                config = json.load(config_file)
            return config
        except FileNotFoundError:
            logging.error(f"Config file not found: {config_path}")
            raise
        except json.JSONDecodeError:
            logging.error(f"Error decoding the config file: {config_path}")
            raise

    async def startWebSocket(self):
        websocket_endpoint = self.config['api_endpoints'].get('websocket_sync', '/websocket-endpoint')
        uri = f"ws://{self.host}:{self.port}{websocket_endpoint}"
        try:
            self.websocket = await websockets.connect(uri)
            self.isRunning = True
            logging.info("WebSocket connection established.")

            await self.receiveData()
        except websockets.ConnectionClosedError as e:
            logging.error(f"WebSocket connection error: {e}")
            await self.handleDisconnection()
        except Exception as e:
            logging.error(f"Unexpected WebSocket error: {e}")
            await self.handleDisconnection()

    async def stopWebSocket(self):
        self.isRunning = False
        if self.websocket:
            try:
                await self.websocket.close()
                logging.info("WebSocket connection closed.")
            except websockets.ConnectionClosedError as e:
                logging.error(f"Error closing WebSocket: {e}")
                QMessageBox.warning(None, "WebSocket Error", "Failed to close the WebSocket properly.")
            except Exception as e:
                logging.error(f"Unexpected error closing WebSocket: {e}")

    async def receiveData(self):
        while self.isRunning:
            try:
                data = await self.websocket.recv()
                if data:
                    message = json.loads(data)
                    await self.handleMessage(message)
            except websockets.ConnectionClosedError as e:
                logging.error(f"WebSocket error: {e}")
                await self.handleDisconnection()
                break
            except json.JSONDecodeError as e:
                logging.error(f"JSON decode error: {e}")
                QMessageBox.warning(None, "Data Error", "Failed to process incoming data from the server.")
            except Exception as e:
                logging.error(f"Unexpected error while receiving WebSocket data: {e}")
                await self.handleDisconnection()
                break

    async def handleMessage(self, message):
        action = message.get("action")

        if action == "disconnect":
            logging.info("Received disconnect message from server.")
            await self.handleDisconnection()
            return

        elementId = message.get("id")
        lastModified = datetime.fromisoformat(message.get("last_modified"))
        createdDate = datetime.fromisoformat(message.get("created_date"))
        path = message.get("path")
        commandId = message.get("command_id")

        success = False
        for attempt in range(5):
            if action == "create_folder":
                success = self.fileSyncManager.createFolder(path, createdDate, lastModified)
            elif action == "destroy":
                success = self.fileSyncManager.destroyElement(path)
            elif action == "download":
                success = await self.fileSyncManager.downloadFile(path, elementId)
            elif action == "upload":
                success = await self.fileSyncManager.uploadFile(path, elementId)
            elif action == "update_metadata":
                success = await self.fileSyncManager.updateMetadata(path, elementId, createdDate, lastModified)

            if success:
                await self.sendSyncStatus(commandId, "OK")
                break
            else:
                await asyncio.sleep(2)

        if not success:
            await self.sendSyncStatus(commandId, "NOK")
            QMessageBox.critical(None, "Sync Error", f"Failed to {action} element {path} after multiple attempts.")
            self.fileSyncManager.resetSync()

    async def sendSyncStatus(self, commandId, status):
        try:
            syncEndpoint = f"{self.host}:{self.port}{self.config['api_endpoints']['websocket_ack'].format(commandId=commandId)}"
            payload = {
                "command_id": commandId,
                "status": status
            }
            await self.fileSyncManager.apiPost(syncEndpoint, payload)
        except Exception as e:
            logging.error(f"Failed to send sync status: {e}")

    async def handleDisconnection(self):
        logging.info("Handling WebSocket disconnection...")
        await self.stopWebSocket()

        DatabaseOperations.clearCredentials(self.tokenManager.dbPath)
        self.tokenManager.accessToken = None
        self.tokenManager.refreshToken = None

        QMessageBox.warning(None, "Disconnected", "The connection to the server has been lost. Please log in again.")

        self.app.resetToLoginState()
