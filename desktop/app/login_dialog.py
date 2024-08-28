from PyQt5.QtWidgets import QDialog, QVBoxLayout, QLabel, QLineEdit, QPushButton, QMessageBox
from .api_requests import ApiRequests
from .encryption_utility import EncryptionUtility
from .database_operations import DatabaseOperations
import logging
import json

class LoginDialog(QDialog):
    def __init__(self, dbPath, config_path):
        super().__init__()
        self.dbPath = dbPath
        self.config = self.load_config(config_path)
        self.setWindowTitle("Login")
        self.setGeometry(100, 100, 300, 150)
        self.key = EncryptionUtility.loadKey()

        layout = QVBoxLayout(self)

        self.usernameLabel = QLabel("Username:")
        layout.addWidget(self.usernameLabel)
        self.usernameEdit = QLineEdit()
        layout.addWidget(self.usernameEdit)

        self.passwordLabel = QLabel("Password:")
        layout.addWidget(self.passwordLabel)
        self.passwordEdit = QLineEdit()
        self.passwordEdit.setEchoMode(QLineEdit.Password)
        layout.addWidget(self.passwordEdit)

        self.loginButton = QPushButton("Login")
        self.loginButton.clicked.connect(self.authenticate)
        layout.addWidget(self.loginButton)

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

    def authenticate(self):
        username = self.usernameEdit.text()
        password = self.passwordEdit.text()
        authEndpoint = f"{self.config['server_url']}{self.config['api_endpoints']['login']}"
        payload = {"username": username, "password": password}

        response = ApiRequests.apiPost(authEndpoint, payload, accessToken=None, refreshToken=None)
        if response:
            self.username = username
            self.password = password
            self.accessToken = response.get("access_token")
            self.refreshToken = response.get("refresh_token")
            encryptedPassword = EncryptionUtility.encryptData(password, self.key)
            if encryptedPassword:
                DatabaseOperations.saveCredentials(self.dbPath, username, encryptedPassword, self.accessToken, self.refreshToken)
                logging.info("User authenticated and credentials saved.")
                self.accept()
            else:
                logging.error("Failed to encrypt credentials.")
                QMessageBox.critical(self, "Error", "Failed to encrypt credentials.")
        else:
            logging.warning("Login failed for user: " + username)
            QMessageBox.warning(self, "Login Failed", "Invalid username or password")
