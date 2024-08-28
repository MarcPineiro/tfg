import os
import logging
import requests
import shutil
from datetime import datetime
from PyQt5.QtWidgets import QMessageBox
from .api_requests import ApiRequests

class FileSyncManager:
    def __init__(self, config, tokenManager):
        self.config = config
        self.tokenManager = tokenManager

    def createFolder(self, path, createdDate, lastModified):
        try:
            if not os.path.exists(path):
                os.makedirs(path)
            os.utime(path, (lastModified.timestamp(), lastModified.timestamp()))
            logging.info(f"Created folder at {path}.")
            return True
        except Exception as e:
            logging.error(f"Error creating folder {path}: {e}")
            return False

    def destroyElement(self, path):
        try:
            if os.path.isdir(path):
                shutil.rmtree(path)
            elif os.path.exists(path):
                os.remove(path)
            logging.info(f"Destroyed element at {path}.")
            return True
        except Exception as e:
            logging.error(f"Error destroying element {path}: {e}")
            return False

    async def downloadFile(self, path, fileId):
        try:
            downloadUrl = f"{self.config['server_url']}{self.config['api_endpoints']['download']}/{fileId}"
            headers = ApiRequests.createCookiesWithToken(self.tokenManager.getAccessToken(), self.tokenManager.getRefreshToken())
            response = requests.get(downloadUrl, headers=headers, stream=True)
            response.raise_for_status()
            with open(path, 'wb') as f:
                for chunk in response.iter_content(chunk_size=8192):
                    if chunk:
                        f.write(chunk)
            logging.info(f"Downloaded file to {path}.")
            return True
        except requests.RequestException as e:
            logging.error(f"Error downloading file {fileId}: {e}")
            return False

    async def uploadFile(self, path, fileId):
        try:
            uploadUrl = f"{self.config['server_url']}{self.config['api_endpoints']['upload']}/{fileId}"
            headers = ApiRequests.createCookiesWithToken(self.tokenManager.getAccessToken(), self.tokenManager.getRefreshToken())
            with open(path, 'rb') as f:
                files = {'file': f}
                response = requests.post(uploadUrl, headers=headers, files=files)
                response.raise_for_status()
            logging.info(f"Uploaded file from {path}.")
            return True
        except requests.RequestException as e:
            logging.error(f"Error uploading file {fileId}: {e}")
            return False

    async def updateMetadata(self, path, fileId, createdDate, lastModified):
        try:
            tempPath = os.path.join(self.config["watched_folder"], "temp", os.path.basename(path))
            if await self.downloadFile(tempPath, fileId):
                os.replace(tempPath, path)
                os.utime(path, (lastModified.timestamp(), lastModified.timestamp()))
                logging.info(f"Updated metadata for {path}.")
                return True
            return False
        except Exception as e:
            logging.error(f"Error updating metadata for {path}: {e}")
            return False

    def resetSync(self):
        QMessageBox.warning(None, "Sync Error", "The sync process encountered an error and has been reset.")
        logging.info("Resetting sync functionality.")
