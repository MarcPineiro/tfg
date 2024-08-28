package edu.udg.tfg.SyncService.config;

import com.netflix.discovery.converters.Auto;
import edu.udg.tfg.SyncService.services.WebSocketConnectionService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.util.Map;
import java.util.Objects;
import java.util.UUID;

@Component
public class UserIdHandshakeInterceptor implements HandshakeInterceptor {

    @Autowired
    private WebSocketConnectionService webSocketConnectionService;

    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler, Map<String, Object> attributes) throws Exception {
        String userId = Objects.requireNonNull(request.getHeaders().get("X-User-Id")).get(0);
        String type = Objects.requireNonNull(request.getHeaders().get("X-client-type")).get(0);
        if (userId != null) {
            attributes.put("userId", userId);
            attributes.put("client", type);
            webSocketConnectionService.connectClient(UUID.fromString(userId), type);
            return true;
        }
        return false;
    }

    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler, Exception exception) {

    }
}