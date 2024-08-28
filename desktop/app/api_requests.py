import logging
import requests
from PyQt5.QtWidgets import QMessageBox

class ApiRequests:
    @staticmethod
    def createCookiesWithToken(accessToken, refreshToken):
        return {
            "access_token": accessToken,
            "refresh_token": refreshToken
        }

    @staticmethod
    def apiPost(url, data, accessToken, refreshToken):
        try:
            cookies = ApiRequests.createCookiesWithToken(accessToken, refreshToken)
            headers = {"Content-Type": "application/json"}
            response = requests.post(url, json=data, headers=headers, cookies=cookies)
            response.raise_for_status()
            logging.debug(f"API POST request successful to {url}.")
            return response.json()
        except requests.RequestException as e:
            logging.error(f"API POST request error: {e}")
            QMessageBox.critical(None, "API Error", "Failed to complete the POST request.")
            return None

    @staticmethod
    def apiGet(url, accessToken, refreshToken):
        try:
            cookies = ApiRequests.createCookiesWithToken(accessToken, refreshToken)
            headers = {"Content-Type": "application/json"}
            response = requests.get(url, headers=headers, cookies=cookies)
            response.raise_for_status()
            logging.debug(f"API GET request successful to {url}.")
            return response.json()
        except requests.RequestException as e:
            logging.error(f"API GET request error: {e}")
            QMessageBox.critical(None, "API Error", "Failed to complete the GET request.")
            return None
