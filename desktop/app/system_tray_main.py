import json
import os
import logging
import sys
from PyQt5.QtWidgets import QMainWindow, QSystemTrayIcon, QMenu, QAction, QVBoxLayout, QScrollArea, QPushButton, QLabel, \
    QWidget, QMessageBox, QHBoxLayout, QDialog
from PyQt5.QtGui import QIcon, QCursor, QGuiApplication
from PyQt5.QtCore import QUrl, QFileSystemWatcher
from PyQt5.QtGui import QDesktopServices
from .database_operations import DatabaseOperations
from .encryption_utility import EncryptionUtility
from .login_dialog import LoginDialog
from .sync_thread import SyncThread
from .settings_dialog import SettingsDialog
from .history_window import HistoryWindow
from .progress_widgets import CustomProgressItem
from .file_sync_manager import FileSyncManager
from .socket_client import SocketClient
from .token_manager import TokenManager
from .utils import getSecureDbPath, setSecureFilePermissions

class SystemTrayMain(QMainWindow):
    def __init__(self, tokenManager):
        super().__init__()
        self.firstSyncDone = False
        self.syncEnabled = True
        self.config = self.loadConfig()
        self.dbPath = getSecureDbPath()
        DatabaseOperations.createTables(self.dbPath)
        self.tokenManager = tokenManager

        self.fileSyncManager = FileSyncManager(self.config, self.tokenManager)

        self.socketClient = SocketClient(
            config_path="./config/config.json",
            tokenManager=self.tokenManager,
            fileSyncManager=self.fileSyncManager,
            app=self
        )

        self.watcher = QFileSystemWatcher([self.config['watched_folder']])
        self.watcher.fileChanged.connect(self.handleFileChange)
        self.watcher.directoryChanged.connect(self.handleFileChange)

        self.setupUi()
        self.setupSystemTray()

        self.syncThread = SyncThread(
            self.config, self.dbPath, self.fileSyncManager,
            self.updateInitialSyncProgress
        )

        if DatabaseOperations.getAutoSyncSetting(self.dbPath):
            self.startSync()

        self.hide()

    @staticmethod
    def loadConfig():
        configFilePath = "./config/config.json"
        try:
            with open(configFilePath, 'r') as configFile:
                configData = json.load(configFile)

            serverUrl = configData.get('server_url')
            socketPort = configData.get('socket_port')
            watchedFolder = configData.get('watched_folder')
            apiEndpoints = configData.get('api_endpoints')
            webUrl = configData.get('web_app_url')

            if not all([serverUrl, socketPort, watchedFolder, apiEndpoints, webUrl]):
                raise ValueError("Incomplete credentials in config file.")

            # os.remove(configFilePath) TODO remove file
            logging.info(f"Credentials successfully retrieved and config file deleted: {configFilePath}")
            return configData

        except (FileNotFoundError, ValueError, json.JSONDecodeError) as e:
            logging.warning(f"Failed to retrieve credentials from config file: {e}")
            return None

    def resetToLoginState(self):
        logging.info("Resetting application to login state.")
        self.syncThread = None
        self.socketClient = None
        self.hide()

        loginDialog = LoginDialog(self.dbPath)
        if loginDialog.exec_() != QDialog.Accepted:
            sys.exit("User closed the application.")
        self.tokenManager = TokenManager(self.dbPath, loginDialog.username, EncryptionUtility.encryptData(loginDialog.password, EncryptionUtility.loadKey()))

        self.socketClient = SocketClient(
            tokenManager=self.tokenManager,
            fileSyncManager=self.fileSyncManager,
            app=self,
            config_path="./config/config.json"
        )
        self.startSync()
        self.show()

    def startSync(self):
        self.syncThread.start()
        logging.info("Synchronization started.")

    def updateInitialSyncProgress(self, item, progress):
        item.updateProgress(progress)

    def handleFileChange(self, path):
        action = {"action": "update", "path": path, "fileId": DatabaseOperations.getFilePath(self.dbPath, path)}
        if self.syncThread.isAlive():
            self.syncThread.enqueueOperation(action)

    def focusOutEvent(self, event):
        self.close()
        event.accept()
        logging.info("Main window closed due to focus loss.")

    def setupUi(self):
        self.setWindowTitle("Google Drive Clone")
        self.setFixedSize(400, 600)

        mainWidget = QWidget(self)
        self.setCentralWidget(mainWidget)
        layout = QVBoxLayout(mainWidget)

        self.progressArea = QScrollArea()
        self.progressArea.setWidgetResizable(True)
        self.progressWidget = QWidget()
        self.progressLayout = QVBoxLayout(self.progressWidget)
        self.progressArea.setWidget(self.progressWidget)
        layout.addWidget(self.progressArea)

        self.recentChangesLabel = QLabel("Recent Changes", self)
        layout.addWidget(self.recentChangesLabel)

        self.recentChangesArea = QScrollArea(self)
        self.recentChangesArea.setWidgetResizable(True)
        self.recentChangesWidget = QWidget()
        self.recentChangesLayout = QVBoxLayout(self.recentChangesWidget)
        self.recentChangesArea.setWidget(self.recentChangesWidget)
        layout.addWidget(self.recentChangesArea)

        self.recentChangesManager = FileSyncManager(self.recentChangesLayout, self.tokenManager)

        #self.historyButton = QPushButton()
        #self.historyButton.setIcon(QIcon("icons/history.png"))
        #self.historyButton.setToolTip("View Full History")
        #self.historyButton.clicked.connect(self.openHistory)
        #layout.addWidget(self.historyButton)

        syncControlLayout = QHBoxLayout()

        self.syncButton = QPushButton()
        self.syncButton.clicked.connect(self.toggleSync)
        syncControlLayout.addWidget(self.syncButton)

        self.restartIcon = QPushButton()
        self.restartIcon.setIcon(QIcon("icons/restart.png"))
        self.restartIcon.setToolTip("Initial Sync Progress")
        self.restartIcon.setVisible(False)
        syncControlLayout.addWidget(self.restartIcon)

        layout.addLayout(syncControlLayout)

        self.folderButton = QPushButton()
        self.folderButton.setIcon(QIcon("icons/folder.png"))
        self.folderButton.clicked.connect(self.openFolder)
        layout.addWidget(self.folderButton)

        self.webButton = QPushButton()
        self.webButton.setIcon(QIcon("icons/web.png"))
        self.webButton.clicked.connect(self.openWeb)
        layout.addWidget(self.webButton)

        self.settingsButton = QPushButton()
        self.settingsButton.setIcon(QIcon("icons/settings.png"))
        self.settingsButton.clicked.connect(self.openSettings)
        layout.addWidget(self.settingsButton)

        self.syncEnabled = True

        with open('config/styles.qss', 'r') as styleFile:
            self.setStyleSheet(styleFile.read())

    def setupSystemTray(self):
        self.trayIcon = QSystemTrayIcon(self)
        self.trayIcon.setIcon(QIcon("icons/sync_on.png"))

        trayMenu = QMenu()

        settingsAction = QAction("Settings", self)
        settingsAction.triggered.connect(self.openSettings)
        trayMenu.addAction(settingsAction)

        #historyAction = QAction("History", self)
        #historyAction.triggered.connect(self.openHistory)
        #trayMenu.addAction(historyAction)

        quitAction = QAction("Close", self)
        quitAction.triggered.connect(self.closeApplication)
        trayMenu.addAction(quitAction)

        self.trayIcon.setContextMenu(trayMenu)
        self.trayIcon.show()

        self.trayIcon.activated.connect(self.onTrayIconClicked)

    def onTrayIconClicked(self, reason):
        if reason == QSystemTrayIcon.Trigger:
            self.showMainWindowNextToTrayIcon()

    def showMainWindowNextToTrayIcon(self):
        trayIconGeometry = self.trayIcon.geometry()
        trayCenter = trayIconGeometry.center()

        screen = QGuiApplication.screenAt(trayCenter)
        if not screen:
            screen = QGuiApplication.primaryScreen()

        screenGeometry = screen.geometry()
        windowSize = self.size()

        x = trayCenter.x() - windowSize.width() // 2
        y = trayIconGeometry.bottom()

        if x + windowSize.width() > screenGeometry.right():
            x = screenGeometry.right() - windowSize.width()

        if x < screenGeometry.left():
            x = screenGeometry.left()

        if y + windowSize.height() > screenGeometry.bottom():
            y = trayIconGeometry.top() - windowSize.height()

        if y < screenGeometry.top():
            y = screenGeometry.top()

        self.move(x, y)
        self.show()
        self.raise_()
        self.activateWindow()

    def toggleSync(self):
        self.syncEnabled = not self.syncEnabled
        self.updateSyncIcon(self.syncEnabled)

        if self.syncEnabled:
            self.startSync()
            logging.info("Sync enabled.")
        else:
            self.socketClient.stopWebSocket()
            DatabaseOperations.clearPendingSync(self.dbPath)
            logging.info("Sync disabled.")

    def updateSyncIcon(self, syncing):
        if syncing:
            self.syncButton.setIcon(QIcon("icons/sync_on.png"))
        else:
            self.syncButton.setIcon(QIcon("icons/sync_off.png"))

    def openFolder(self):
        folderPath = self.config['watched_folder']
        try:
            QDesktopServices.openUrl(QUrl.fromLocalFile(folderPath))
            logging.info(f"Opened folder: {folderPath}.")
        except Exception as e:
            logging.error(f"Error opening folder: {e}")
            QMessageBox.critical(self, "Folder Error", "Failed to open the folder.")

    def openWeb(self):
        webUrl = self.config['web_app_url']
        try:
            QDesktopServices.openUrl(QUrl(webUrl))
            logging.info(f"Opened web page: {webUrl}.")
        except Exception as e:
            logging.error(f"Error opening web page: {e}")
            QMessageBox.critical(self, "Web Page Error", "Failed to open the web page.")

    def openSettings(self):
        try:
            if not hasattr(self, 'settingsDialog') or not self.settingsDialog.isVisible():
                self.settingsDialog = SettingsDialog(self.config, self.dbPath)
                self.settingsDialog.resyncRequired.connect(self.resyncApplication)
                self.settingsDialog.show()
                logging.info("Settings dialog opened.")
            else:
                self.settingsDialog.raise_()
                self.settingsDialog.activateWindow()
        except Exception as e:
            logging.error(f"Error opening settings: {e}")
            QMessageBox.critical(self, "Settings Error", "Failed to open settings.")

    def resyncApplication(self):
        self.syncEnabled = False
        self.syncButton.setVisible(False)
        self.progressWidget.setEnabled(False)

        self.progressArea.setVisible(True)
        self.restartIcon.setVisible(True)
        self.restartIcon.setToolTip("Re-syncing...")

        self.syncThread = SyncThread(self.config, self.dbPath, self.fileSyncManager, self.updateInitialSyncProgress, self.handleSyncCompletion)
        self.syncThread.start()
        self.syncThread.finished.connect(self.onResyncComplete)

    def onResyncComplete(self):
        self.syncButton.setVisible(True)
        self.progressWidget.setEnabled(True)
        self.syncEnabled = True
        self.firstSyncDone = True
        logging.info("Re-sync completed and functionalities restored.")

    def closeApplication(self):
        logging.info("Application is closing.")
        self.trayIcon.hide()
        self.close()
        sys.exit()
