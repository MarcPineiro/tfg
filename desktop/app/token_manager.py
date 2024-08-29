import logging
from PyQt5.QtCore import QTimer
from PyQt5.QtWidgets import QMessageBox
from .api_requests import ApiRequests
from .database_operations import DatabaseOperations
from .encryption_utility import EncryptionUtility
import json

class TokenManager:
    def __init__(self, dbPath, username, encryptedPassword, config_path):
        self.dbPath = dbPath
        self.config = self.load_config(config_path)
        self.username = username
        self.encryptedPassword = encryptedPassword
        self.accessToken = None
        self.refreshToken = None

        self.refreshTimer = QTimer()
        self.refreshTimer.timeout.connect(self.refreshTokens)
        #self.refreshTimer.start(9 * 60 * 1000)
        self.refreshTimer.start(1 * 60 * 1000)

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

    def refreshTokens(self):
        refreshEndpoint = f"{self.config['server_url']}{self.config['api_endpoints']['refresh_tokens']}"
        payload = {
            "username": self.username,
            "refresh_token": self.refreshToken
        }

        response = ApiRequests.apiPost(
            refreshEndpoint, payload,
            accessToken=None, refreshToken=self.refreshToken
        )

        if response:
            self.accessToken = response.get("access_token")
            self.refreshToken = response.get("refresh_token")

            DatabaseOperations.saveCredentials(self.dbPath, self.username, self.encryptedPassword, self.accessToken, self.refreshToken)
            logging.info("Tokens refreshed successfully.")
        else:
            logging.error("Failed to refresh tokens.")
            QMessageBox.critical(None, "Token Refresh Failed", "Unable to refresh tokens. Please log in again.")
            self.handleTokenFailure()

    def handleTokenFailure(self):
        DatabaseOperations.clearCredentials(self.dbPath)
        self.accessToken = None
        self.refreshToken = None
        QMessageBox.warning(None, "Authentication Error", "The session has expired. Please log in again.")
        self.app.resetToLoginState()

    def getAccessToken(self):
        return self.accessToken

    def getRefreshToken(self):
        return self.refreshToken

    def authenticate(self):
        username = self.username
        password = EncryptionUtility.decryptData(self.encryptedPassword, self.key)
        authEndpoint = f"{self.config['server_url']}{self.config['api_endpoints']['login']}"
        payload = {"username": username, "password": password}

        response = ApiRequests.apiPost(authEndpoint, payload, accessToken=None, refreshToken=None)
        if response:
            self.accessToken = response.get("access_token")
            self.refreshToken = response.get("refresh_token")
            encryptedPassword = EncryptionUtility.encryptData(password, self.key)
            if encryptedPassword:
                DatabaseOperations.saveCredentials(self.dbPath, username, encryptedPassword, self.accessToken, self.refreshToken)
                logging.info("User authenticated and credentials saved.")
            else:
                logging.error("Failed to encrypt credentials.")
                QMessageBox.critical(None, "Error", "Failed to encrypt credentials.")
        else:
            logging.warning("Login failed for user: " + username)
            QMessageBox.warning(None, "Login Failed", "Invalid username or password")
