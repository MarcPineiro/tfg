from PyQt5.QtWidgets import QDialog, QVBoxLayout, QListWidget, QListWidgetItem, QMessageBox
from .api_requests import ApiRequests

class HistoryWindow(QDialog):
    def __init__(self, config, tokenManager):
        super().__init__()
        self.config = config
        self.tokenManager = tokenManager
        self.setWindowTitle("File Change History")
        self.setGeometry(300, 300, 500, 400)

        self.layout = QVBoxLayout(self)

        self.historyList = QListWidget(self)
        self.layout.addWidget(self.historyList)

        self.loadHistory(page=1)

    def loadHistory(self, page):
        historyEndpoint = f"{self.config['server_url']}{self.config['api_endpoints']['history'].format(userId=self.tokenManager.username)}?page={page}"
        historyData = ApiRequests.apiGet(historyEndpoint, self.tokenManager.getAccessToken(), self.tokenManager.getRefreshToken())

        if historyData:
            for entry in historyData.get('entries', []):
                listItem = QListWidgetItem(f"{entry['timestamp']}: {entry['action']} - {entry['path']}")
                self.historyList.addItem(listItem)
        else:
            QMessageBox.warning(self, "History Error", "Failed to load history data.")
