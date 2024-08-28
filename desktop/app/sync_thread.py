import os
import threading
import time
import logging
from queue import Queue
from PyQt5.QtWidgets import QMessageBox
from .database_operations import DatabaseOperations
from .progress_widgets import CustomProgressItem
from .file_sync_manager import FileSyncManager
from .api_requests import ApiRequests


class SyncThread(threading.Thread):
    def __init__(self, config, dbPath, fileSyncManager, progressCallback):
        super().__init__()
        self.config = config
        self.dbPath = dbPath
        self.fileSyncManager = fileSyncManager
        self.progressCallback = progressCallback
        self.maxConcurrentOperations = DatabaseOperations.getMaxConcurrentOperations(dbPath)
        self.operationQueue = Queue()
        self.activeOperations = []
        self.pausedOperations = []
        self.lock = threading.Lock()

    def run(self):
        try:
            accessToken = self.fileSyncManager.tokenManager.getAccessToken()
            refreshToken = self.fileSyncManager.tokenManager.getRefreshToken()

            serverFiles = ApiRequests.apiGet(
                f"{self.config['server_url']}{self.config['api_endpoints']['files']}",
                accessToken, refreshToken
            )
            if serverFiles is None:
                return

            localDirectory = self.config["watched_folder"]

            initialSyncPlan = self.fileSyncManager.compareAndGenerateSyncPlan(serverFiles, localDirectory)

            pendingSyncs = DatabaseOperations.getPendingSync(self.dbPath)

            mergedSyncPlan = self.mergeSyncPlans(initialSyncPlan, pendingSyncs)

            for action in mergedSyncPlan:
                if action["action"] in ["create_folder", "delete"]:
                    self.processImmediateAction(action)
                else:
                    self.enqueueOperation(action)

            self.processOperationsConcurrently()
        except Exception as e:
            logging.error(f"Error during sync process: {e}")
            QMessageBox.critical(None, "Sync Error", "Failed to complete the synchronization process.")

    def processImmediateAction(self, action):
        actionType = action["action"]
        path = os.path.join(self.config["watched_folder"], action["path"])

        if actionType == "create_folder":
            os.makedirs(path, exist_ok=True)
            logging.info(f"Created folder at {path}.")
        elif actionType == "delete":
            if os.path.exists(path):
                if os.path.isdir(path):
                    os.rmdir(path)
                else:
                    os.remove(path)
                logging.info(f"Deleted {path}.")
            else:
                logging.warning(f"Path not found for deletion: {path}.")

    def enqueueOperation(self, action):
        self.operationQueue.put(action)

    def processOperationsConcurrently(self):
        while not self.operationQueue.empty():
            if len(self.activeOperations) < self.maxConcurrentOperations:
                action = self.operationQueue.get()
                self.startOperation(action)
            else:
                time.sleep(0.1)

    def startOperation(self, action):
        actionType = action["action"]
        path = os.path.join(self.config["watched_folder"], action["path"])

        item = CustomProgressItem(filename=path, action=actionType, onPause=self.pauseOperation,
                                  onResume=self.resumeOperation)
        item.start()
        self.progressCallback(item, 0)

        thread = threading.Thread(target=self.executeOperation, args=(item, action))
        thread.start()

    def executeOperation(self, item, action):
        with self.lock:
            self.activeOperations.append(item)
        try:
            if action["action"] == "download":
                self.fileSyncManager.createFile(item.filename, action["fileId"])
            elif action["action"] == "upload":
                self.fileSyncManager.updateFile(item.filename, action["fileId"])

            item.updateProgress(100)
            item.markCompleted()
        finally:
            with self.lock:
                self.activeOperations.remove(item)
            self.processOperationsConcurrently()

    def pauseOperation(self, item):
        with self.lock:
            if item in self.activeOperations:
                self.activeOperations.remove(item)
                self.pausedOperations.append(item)
                logging.info(f"Operation paused: {item.filename}")
                self.processOperationsConcurrently()

    def resumeOperation(self, item):
        with self.lock:
            if item in self.pausedOperations:
                self.pausedOperations.remove(item)
                if len(self.activeOperations) < self.maxConcurrentOperations:
                    self.activeOperations.append(item)
                    logging.info(f"Operation resumed: {item.filename}")
                    self.startOperation(item.action)
                else:
                    self.operationQueue.put(item.action)
                    logging.info(f"Operation enqueued: {item.filename}")
