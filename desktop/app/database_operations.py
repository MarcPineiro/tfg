import os
import sqlite3
import logging
import sys
from PyQt5.QtWidgets import QMessageBox

class DatabaseOperations:
    @staticmethod
    def createTables(dbPath):
        try:
            with sqlite3.connect(dbPath) as conn:
                c = conn.cursor()
                c.execute('''CREATE TABLE IF NOT EXISTS credentials
                             (username TEXT, encryptedPassword TEXT, accessToken TEXT, refreshToken TEXT)''')
                c.execute('''CREATE TABLE IF NOT EXISTS filePaths
                             (fileId TEXT PRIMARY KEY, path TEXT)''')
                c.execute('''CREATE TABLE IF NOT EXISTS pendingSync
                             (path TEXT PRIMARY KEY, action TEXT)''')
                c.execute('''CREATE TABLE IF NOT EXISTS settings
                             (autoSync INTEGER, maxConcurrentOperations INTEGER DEFAULT 5)''')
                conn.commit()
                logging.info("Database tables created or verified successfully.")
        except sqlite3.Error as e:
            logging.critical(f"Database error during table creation: {e}")
            QMessageBox.critical(None, "Database Error", "Unable to initialize the database.")
            sys.exit(1)

    @staticmethod
    def getCredentials(dbPath):
        try:
            with sqlite3.connect(dbPath) as conn:
                c = conn.cursor()
                c.execute("SELECT * FROM credentials")
                return c.fetchone()
        except sqlite3.Error as e:
            logging.error(f"Database error retrieving credentials: {e}")
            return None

    @staticmethod
    def saveCredentials(dbPath, username, encryptedPassword, accessToken, refreshToken):
        try:
            with sqlite3.connect(dbPath) as conn:
                c = conn.cursor()
                c.execute("INSERT OR REPLACE INTO credentials VALUES (?, ?, ?, ?)",
                          (username, encryptedPassword, accessToken, refreshToken))
                conn.commit()
                logging.info("Credentials saved to database successfully.")
        except sqlite3.Error as e:
            logging.error(f"Database error saving credentials: {e}")
            QMessageBox.warning(None, "Database Error", "Failed to save credentials.")

    @staticmethod
    def saveFilePath(dbPath, fileId, path):
        try:
            with sqlite3.connect(dbPath) as conn:
                c = conn.cursor()
                c.execute("INSERT OR REPLACE INTO filePaths VALUES (?, ?)", (fileId, path))
                conn.commit()
                logging.info(f"File path saved to database: {path} with fileId {fileId}.")
        except sqlite3.Error as e:
            logging.error(f"Database error saving file path: {e}")
            QMessageBox.warning(None, "Database Error", "Failed to save file path.")

    @staticmethod
    def getFilePath(dbPath, fileId):
        try:
            with sqlite3.connect(dbPath) as conn:
                c = conn.cursor()
                c.execute("SELECT path FROM filePaths WHERE fileId = ?", (fileId,))
                result = c.fetchone()
                return result[0] if result else None
        except sqlite3.Error as e:
            logging.error(f"Database error retrieving file path: {e}")
            return None

    @staticmethod
    def savePendingSync(dbPath, path, action):
        try:
            with sqlite3.connect(dbPath) as conn:
                c = conn.cursor()
                c.execute("INSERT OR REPLACE INTO pendingSync VALUES (?, ?)", (path, action))
                conn.commit()
                logging.info(f"Pending sync action saved for path: {path}, action: {action}.")
        except sqlite3.Error as e:
            logging.error(f"Database error saving pending sync: {e}")
            QMessageBox.warning(None, "Database Error", "Failed to save pending sync action.")

    @staticmethod
    def getPendingSync(dbPath):
        try:
            with sqlite3.connect(dbPath) as conn:
                c = conn.cursor()
                c.execute("SELECT * FROM pendingSync")
                return c.fetchall()
        except sqlite3.Error as e:
            logging.error(f"Database error retrieving pending sync actions: {e}")
            return []

    @staticmethod
    def clearPendingSync(dbPath):
        try:
            with sqlite3.connect(dbPath) as conn:
                c = conn.cursor()
                c.execute("DELETE FROM pendingSync")
                conn.commit()
                logging.info("Pending sync actions cleared from database.")
        except sqlite3.Error as e:
            logging.error(f"Database error clearing pending sync actions: {e}")
            QMessageBox.warning(None, "Database Error", "Failed to clear pending sync actions.")

    @staticmethod
    def getAutoSyncSetting(dbPath):
        try:
            with sqlite3.connect(dbPath) as conn:
                c = conn.cursor()
                c.execute("SELECT autoSync FROM settings")
                result = c.fetchone()
                return bool(result[0]) if result else True
        except sqlite3.Error as e:
            logging.error(f"Database error retrieving auto-sync setting: {e}")
            return True

    @staticmethod
    def saveAutoSyncSetting(dbPath, autoSync):
        try:
            with sqlite3.connect(dbPath) as conn:
                c = conn.cursor()
                c.execute("INSERT OR REPLACE INTO settings VALUES (?)", (1 if autoSync else 0,))
                conn.commit()
                logging.info("Auto-sync setting saved to database.")
        except sqlite3.Error as e:
            logging.error(f"Database error saving autoSync setting: {e}")
            QMessageBox.warning(None, "Database Error", "Failed to save auto-sync setting.")

    @staticmethod
    def getMaxConcurrentOperations(dbPath):
        try:
            with sqlite3.connect(dbPath) as conn:
                c = conn.cursor()
                c.execute("SELECT maxConcurrentOperations FROM settings")
                result = c.fetchone()
                return result[0] if result else 5
        except sqlite3.Error as e:
            logging.error(f"Database error retrieving maxConcurrentOperations setting: {e}")
            return 5

    @staticmethod
    def saveMaxConcurrentOperations(dbPath, maxConcurrentOperations):
        try:
            with sqlite3.connect(dbPath) as conn:
                c = conn.cursor()
                c.execute("UPDATE settings SET maxConcurrentOperations = ?", (maxConcurrentOperations,))
                conn.commit()
                logging.info("Max concurrent operations setting saved to database.")
        except sqlite3.Error as e:
            logging.error(f"Database error saving maxConcurrentOperations setting: {e}")
            QMessageBox.warning(None, "Database Error", "Failed to save max concurrent operations setting.")

    @staticmethod
    def clearCredentials(dbPath):
        try:
            with sqlite3.connect(dbPath) as conn:
                c = conn.cursor()
                c.execute("DELETE FROM credentials")
                conn.commit()
                logging.info("Credentials cleared from the database.")
        except sqlite3.Error as e:
            logging.error(f"Database error clearing credentials: {e}")
