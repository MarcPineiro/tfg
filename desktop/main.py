import json
import os
import sys
import logging
import asyncio

from PyQt5.QtWidgets import QApplication, QMessageBox, QDialog
from app.database_operations import DatabaseOperations
from app.encryption_utility import EncryptionUtility
from app.login_dialog import LoginDialog
from app.token_manager import TokenManager
from app.system_tray_main import SystemTrayMain
from app.utils import getSecureDbPath, setSecureFilePermissions


def retrieveCredentialsFromFile(configFilePath):
    try:
        with open(configFilePath, 'r') as configFile:
            configData = json.load(configFile)

        username = configData.get('username')
        password = configData.get('password')

        if not all([username, password]):
            raise ValueError("Incomplete credentials in config file.")

        # os.remove(configFilePath) TODO remove file
        logging.info(f"Credentials successfully retrieved and config file deleted: {configFilePath}")
        return username, password,

    except (FileNotFoundError, ValueError, json.JSONDecodeError) as e:
        logging.warning(f"Failed to retrieve credentials from config file: {e}")
        return None


def main():
    app = QApplication(sys.argv)

    username, password = None, None

    dbPath = getSecureDbPath()
    if not os.path.exists(dbPath):
        DatabaseOperations.createTables(dbPath)
    # setSecureFilePermissions(dbPath) TODO seguro que lo quiero hacer?

    configFilePath = "../config/credentials_config.json"
    credentials = retrieveCredentialsFromFile(configFilePath)

    if credentials:
        username, password = credentials
    else:
        credentials = DatabaseOperations.getCredentials(dbPath)
        if credentials:
            username, encryptedPassword = credentials
            key = EncryptionUtility.loadKey()
            password = EncryptionUtility.decryptData(encryptedPassword, key)
            if not password:
                logging.error("Error decrypting password.")
                QMessageBox.critical(None, "Decryption Error", "Failed to decrypt password. Exiting.")
                sys.exit(1)

    #if not username or not password:
        #    loginDialog = LoginDialog(dbPath, "./config/config.json")
        #if loginDialog.exec_() != QDialog.Accepted:
        #    sys.exit("Could not authenticate user.")
        #username = loginDialog.username
        #password = loginDialog.password

    username = "test"
    password = "test"

    tokenManager = TokenManager(dbPath, username, EncryptionUtility.encryptData(password, EncryptionUtility.loadKey()), "./config/config.json")

    try:
        window = SystemTrayMain(tokenManager)
        sys.exit(app.exec_())
    except Exception as e:
        logging.critical(f"Critical error: {e}")
        QMessageBox.critical(None, "Critical Error", "The application encountered a critical error and will close.")
        sys.exit(1)


if __name__ == "__main__":
    main()
