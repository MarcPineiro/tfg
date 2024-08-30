package edu.udg.tfg.Gateway.config;

import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;

@Configuration
public class GatewayConfig {

    @Bean
    public RouteLocator routes(RouteLocatorBuilder builder, JwtAuthenticationFilter jwtAuthenticationFilter) {
        return builder.routes()

                // Global handling for OPTIONS requests to bypass authentication
                .route("options_request", r -> r.method(HttpMethod.OPTIONS)
                        .filters(f -> f.setResponseHeader("Access-Control-Allow-Origin", "*")
                                .setResponseHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS")
                                .setResponseHeader("Access-Control-Allow-Headers", "*")
                                .setResponseHeader("Access-Control-Expose-Headers", "Authorization, X-Refresh-Token, X-Client-Type")
                                .setResponseHeader("Access-Control-Allow-Credentials", "true"))
                        .uri("no://op"))

                // File Management Service Routes
                .route("files_root_get", r -> r.path("/files/root")
                        .and().method("GET")
                        .filters(f -> f.filter(jwtAuthenticationFilter))
                        .uri("lb://FileManagement"))

                .route("files_structure_get", r -> r.path("/files/structure")
                        .and().method("GET")
                        .filters(f -> f.filter(jwtAuthenticationFilter))
                        .uri("lb://FileManagement"))

                .route("files_element_get", r -> r.path("/files/{elementId}")
                        .and().method("GET")
                        .filters(f -> f.filter(jwtAuthenticationFilter)
                                .rewritePath("/files/(?<elementId>.*)", "/files/${elementId}"))
                        .uri("lb://FileManagement"))

                .route("files_element_download_get", r -> r.path("/files/{elementId}/download")
                        .and().method("GET")
                        .filters(f -> f.filter(jwtAuthenticationFilter)
                                .rewritePath("/files/(?<elementId>.*)/download", "/files/${elementId}/download"))
                        .uri("lb://FileManagement"))

                .route("files_by_folder_get", r -> r.path("/files/{folderId}/files")
                        .and().method("GET")
                        .filters(f -> f.filter(jwtAuthenticationFilter)
                                .rewritePath("/files/(?<folderId>.*)/files", "/files/${folderId}/files"))
                        .uri("lb://FileManagement"))

                .route("folders_by_folder_get", r -> r.path("/files/{folderId}/folders")
                        .and().method("GET")
                        .filters(f -> f.filter(jwtAuthenticationFilter)
                                .rewritePath("/files/(?<folderId>.*)/folders", "/files/${folderId}/folders"))
                        .uri("lb://FileManagement"))

                .route("files_post", r -> r.path("/files/")
                        .and().method("POST")
                        .filters(f -> f.filter(jwtAuthenticationFilter))
                        .uri("lb://FileManagement"))

                .route("files_root_post", r -> r.path("/files/root")
                        .and().method("POST")
                        .filters(f -> f.filter(jwtAuthenticationFilter))
                        .uri("lb://FileManagement"))

                .route("files_element_put", r -> r.path("/files/{elementId}")
                        .and().method("PUT")
                        .filters(f -> f.filter(jwtAuthenticationFilter)
                                .rewritePath("/files/(?<elementId>.*)", "/files/${elementId}"))
                        .uri("lb://FileManagement"))

                .route("files_move_put", r -> r.path("/files/{elementId}/move/{folderId}")
                        .and().method("PUT")
                        .filters(f -> f.filter(jwtAuthenticationFilter)
                                .rewritePath("/files/(?<elementId>.*)/move/(?<folderId>.*)", "/files/${elementId}/move/${folderId}"))
                        .uri("lb://FileManagement"))

                .route("files_element_delete", r -> r.path("/files/{elementId}")
                        .and().method("DELETE")
                        .filters(f -> f.filter(jwtAuthenticationFilter)
                                .rewritePath("/files/(?<elementId>.*)", "/files/${elementId}"))
                        .uri("lb://FileManagement"))

                // File Sharing Service Routes
                .route("share_root_get", r -> r.path("/share/root")
                        .and().method("GET")
                        .filters(f -> f.filter(jwtAuthenticationFilter))
                        .uri("lb://FileManagement"))

                .route("share_structure_get", r -> r.path("/share/structure")
                        .and().method("GET")
                        .filters(f -> f.filter(jwtAuthenticationFilter))
                        .uri("lb://FileManagement"))

                .route("share_post", r -> r.path("/share/")
                        .and().method("POST")
                        .filters(f -> f.filter(jwtAuthenticationFilter))
                        .uri("lb://FileManagement"))

                .route("share_delete", r -> r.path("/share/")
                        .and().method("DELETE")
                        .filters(f -> f.filter(jwtAuthenticationFilter))
                        .uri("lb://FileManagement"))

                // Trash Service Routes
                .route("trash_root_get", r -> r.path("/trash/root")
                        .and().method("GET")
                        .filters(f -> f.filter(jwtAuthenticationFilter))
                        .uri("lb://FileManagement"))

                .route("trash_structure_get", r -> r.path("/trash/structure")
                        .and().method("GET")
                        .filters(f -> f.filter(jwtAuthenticationFilter))
                        .uri("lb://FileManagement"))

                .route("trash_element_get", r -> r.path("/trash/{elementId}")
                        .and().method("GET")
                        .filters(f -> f.filter(jwtAuthenticationFilter)
                                .rewritePath("/trash/(?<elementId>.*)", "/trash/${elementId}"))
                        .uri("lb://FileManagement"))

                .route("trash_element_delete", r -> r.path("/trash/{elementId}")
                        .and().method("DELETE")
                        .filters(f -> f.filter(jwtAuthenticationFilter)
                                .rewritePath("/trash/(?<elementId>.*)", "/trash/${elementId}"))
                        .uri("lb://FileManagement"))

                .route("trash_restore_put", r -> r.path("/trash/{elementId}/restore")
                        .and().method("PUT")
                        .filters(f -> f.filter(jwtAuthenticationFilter)
                                .rewritePath("/trash/(?<elementId>.*)/restore", "/trash/${elementId}/restore"))
                        .uri("lb://FileManagement"))

                // User Authentication Routes
                .route("user_login_post", r -> r.path("/users/auth/login")
                        .and().method("POST")
                        .filters(f -> f.removeRequestHeader(HttpHeaders.AUTHORIZATION)
                                .retry(3))
                        .uri("lb://UserAuthentication"))

                .route("user_register_post", r -> r.path("/users/auth/register")
                        .and().method("POST")
                        .filters(f -> f.removeRequestHeader(HttpHeaders.AUTHORIZATION)
                                .retry(3))
                        .uri("lb://UserAuthentication"))

                .route("user_keep_alive_post", r -> r.path("/users/auth/keep-alive")
                        .and().method("POST")
                        .filters(f -> f.removeRequestHeader(HttpHeaders.AUTHORIZATION)
                                .retry(3))
                        .uri("lb://UserAuthentication"))

                .route("user_check_post", r -> r.path("/users/auth/check")
                        .and().method("POST")
                        .filters(f -> f.removeRequestHeader(HttpHeaders.AUTHORIZATION)
                                .retry(3))
                        .uri("lb://UserAuthentication"))

                // User Management Routes
                .route("user_get", r -> r.path("/users/")
                        .and().method("GET")
                        .filters(f -> f.filter(jwtAuthenticationFilter)
                                .retry(3))
                        .uri("lb://UserManagement"))

                .route("user_id_get", r -> r.path("/users/{username}/id")
                        .and().method("GET")
                        .filters(f -> f.filter(jwtAuthenticationFilter)
                                .rewritePath("/users/(?<username>.*)/id", "/users/${username}/id")
                                .retry(3))
                        .uri("lb://UserManagement"))

                .route("user_put", r -> r.path("/users/")
                        .and().method("PUT")
                        .filters(f -> f.filter(jwtAuthenticationFilter)
                                .retry(3))
                        .uri("lb://UserManagement"))

                .route("user_delete", r -> r.path("/users/")
                        .and().method("DELETE")
                        .filters(f -> f.filter(jwtAuthenticationFilter)
                                .retry(3))
                        .uri("lb://UserManagement"))

                // WebSocket Route
                .route("websocket_sync_post", r -> r.path("/websocket/sync")
                        .and().method("POST")
                        .filters(f -> f.setRequestHeader(HttpHeaders.UPGRADE, "websocket")
                                .setRequestHeader(HttpHeaders.CONNECTION, "Upgrade")
                                .filter(jwtAuthenticationFilter))
                        .uri("lb://SyncService"))

                .route("websocket_ack_post", r -> r.path("/websocket/ack/{commandId}")
                        .and().method("POST")
                        .filters(f -> f.setRequestHeader(HttpHeaders.UPGRADE, "websocket")
                                .setRequestHeader(HttpHeaders.CONNECTION, "Upgrade")
                                .filter(jwtAuthenticationFilter)
                                .rewritePath("/websocket/ack/(?<commandId>.*)", "/websocket/ack/${commandId}"))
                        .uri("lb://SyncService"))

                .build();
    }
}
