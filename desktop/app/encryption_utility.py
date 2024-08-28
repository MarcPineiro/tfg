import os
from cryptography.fernet import Fernet
import logging

class EncryptionUtility:
    @staticmethod
    def loadKey():
        keyPath = "key.key"
        if os.path.exists(keyPath):
            with open(keyPath, "rb") as keyFile:
                return keyFile.read()
        else:
            key = Fernet.generate_key()
            with open(keyPath, "wb") as keyFile:
                keyFile.write(key)
            logging.info("Encryption key generated and saved.")
            return key

    @staticmethod
    def encryptData(data, key):
        fernet = Fernet(key)
        return fernet.encrypt(data.encode())

    @staticmethod
    def decryptData(encryptedData, key):
        fernet = Fernet(key)
        return fernet.decrypt(encryptedData).decode()
