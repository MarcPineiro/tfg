package edu.udg.tfg.SyncService.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class WebSocketConnectionService {

    private SimpMessagingTemplate messagingTemplate;

    public WebSocketConnectionService(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    private final Set<UUID> connectedDesktop = new HashSet<>();
    private final Set<UUID> connectedWeb = new HashSet<>();

    private final String DESCONECTION_DESKTOP = "/queue/desktop/logoff";
    private final String DESCONECTION_WEB = "/queue/web/logoff";

    public void connectClient(UUID userId, String type) {
        String destination = DESCONECTION_DESKTOP;
        Set<UUID> client = connectedDesktop;
        if(Objects.equals(type, "web")) {
            destination = DESCONECTION_WEB;
            client = connectedWeb;
        }
        synchronized (client) {
            if (client.contains(userId)) {
                notifyExistingConnection(userId, destination);
            }
            client.add(userId);
        }
    }

    public boolean disconnectClient(UUID userId, String type) {
        String destination = DESCONECTION_DESKTOP;
        Set<UUID> client = connectedDesktop;
        if(Objects.equals(type, "web")) {
            destination = DESCONECTION_WEB;
            client = connectedWeb;
        }
        synchronized (client) {
            boolean wasConnected = client.remove(userId);
            if (wasConnected) {
                notifyExistingConnection(userId, destination);
            }
            return wasConnected;
        }
    }

    public boolean isDesktopClientConnected(UUID userId) {
        return connectedDesktop.contains(userId);
    }

    private void notifyExistingConnection(UUID userId, String destination) {
        messagingTemplate.convertAndSendToUser(
                userId.toString(),
                destination,
                "logoff"
        );
    }
}
