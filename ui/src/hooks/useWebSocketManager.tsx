import { useEffect } from 'react';
import { getWebSocketConnection } from '../utils/api';

const useWebSocketManager = (refreshItems: (parentFolderId:string) => void, logout: () => void) => {
    useEffect(() => {
        const socket = getWebSocketConnection();

        socket.onmessage = (event) => {
            const message = JSON.parse(event.data);
            if (message.route === '/queue/web/reply') {
                refreshItems(message.parentFolderId);
            } else if (message.route === '/queue/web/logoff') {
                logout();
            }
        };

        return () => {
            socket.close();
        };
    }, [refreshItems, logout]);
};

export default useWebSocketManager;
