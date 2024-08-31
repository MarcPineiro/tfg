import os
import time
import logging
from PyQt5.QtWidgets import QWidget, QVBoxLayout, QLabel, QProgressBar, QPushButton, QMessageBox
from PyQt5.QtCore import QUrl
from PyQt5.QtGui import QIcon, QDesktopServices


class CustomProgressItem(QWidget):
    def __init__(self, filename, action, onPause, onResume):
        super().__init__()
        self.filename = filename
        self.action = action
        self.startTime = None
        self.endTime = None
        self.onPause = onPause
        self.onResume = onResume
        self.paused = False

        layout = QVBoxLayout(self)
        self.label = QLabel(f"{filename} - {action.capitalize()}")
        layout.addWidget(self.label)
        self.progressBar = QProgressBar(self)
        layout.addWidget(self.progressBar)

        self.pauseButton = QPushButton("Pause")
        self.pauseButton.clicked.connect(self.togglePauseResume)
        layout.addWidget(self.pauseButton)

        self.setFixedHeight(60)

    def start(self):
        self.startTime = time.time()

    def updateProgress(self, value):
        self.progressBar.setValue(value)
        logging.debug(f"Progress updated for {self.filename}: {value}%.")

    def markCompleted(self):
        self.endTime = time.time()
        timeTaken = self.endTime - self.startTime
        minutes, seconds = divmod(timeTaken, 60)
        timeDisplay = f"{int(minutes)}m {int(seconds)}s"

        self.label.setText(f"{self.filename} - {self.action.capitalize()} completed in {timeDisplay}")
        self.progressBar.setValue(100)
        self.progressBar.setEnabled(False)
        self.pauseButton.setEnabled(False)
        self.label.setStyleSheet("color: gray;")

        self.folderButton = QPushButton()
        current_directory = os.path.dirname(os.path.realpath(__file__))
        self.folderButton.setIcon(QIcon(os.path.join(current_directory, "icons/folder.png")))
        self.folderButton.setToolTip("Open containing folder")
        self.folderButton.clicked.connect(self.openFolder)
        self.folderButton.setFixedSize(30, 30)

        layout = self.layout()
        layout.addWidget(self.folderButton)

    def togglePauseResume(self):
        if self.paused:
            self.onResume(self)
            self.pauseButton.setText("Pause")
        else:
            self.onPause(self)
            self.pauseButton.setText("Resume")
        self.paused = not self.paused

    def openFolder(self):
        folderPath = os.path.dirname(self.filename)
        try:
            QDesktopServices.openUrl(QUrl.fromLocalFile(folderPath))
            logging.info(f"Opened folder: {folderPath}.")
        except Exception as e:
            logging.error(f"Error opening folder: {e}")
            QMessageBox.critical(self, "Folder Error", "Failed to open the folder.")
