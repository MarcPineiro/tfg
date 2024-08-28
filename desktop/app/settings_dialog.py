from PyQt5.QtWidgets import QDialog, QVBoxLayout, QLabel, QCheckBox, QSpinBox, QPushButton
from PyQt5.QtCore import pyqtSignal
from .database_operations import DatabaseOperations

class SettingsDialog(QDialog):
    resyncRequired = pyqtSignal()

    def __init__(self, config, dbPath):
        super().__init__()
        self.config = config
        self.dbPath = dbPath

        self.setWindowTitle("Settings")
        self.setGeometry(100, 100, 400, 300)

        layout = QVBoxLayout(self)

        self.autoSyncCheckbox = QCheckBox("Enable Auto-Sync on Startup")
        self.autoSyncCheckbox.setChecked(DatabaseOperations.getAutoSyncSetting(self.dbPath))
        layout.addWidget(self.autoSyncCheckbox)

        self.maxConcurrentOperationsLabel = QLabel("Max Concurrent Uploads/Downloads:")
        layout.addWidget(self.maxConcurrentOperationsLabel)
        self.maxConcurrentOperationsSpinbox = QSpinBox()
        self.maxConcurrentOperationsSpinbox.setRange(1, 10)
        self.maxConcurrentOperationsSpinbox.setValue(DatabaseOperations.getMaxConcurrentOperations(self.dbPath))
        layout.addWidget(self.maxConcurrentOperationsSpinbox)

        saveButton = QPushButton("Save")
        saveButton.clicked.connect(self.saveSettings)
        layout.addWidget(saveButton)

    def saveSettings(self):
        autoSync = self.autoSyncCheckbox.isChecked()
        maxConcurrentOperations = self.maxConcurrentOperationsSpinbox.value()
        DatabaseOperations.saveAutoSyncSetting(self.dbPath, autoSync)
        DatabaseOperations.saveMaxConcurrentOperations(self.dbPath, maxConcurrentOperations)

        self.resyncRequired.emit()
        self.accept()
