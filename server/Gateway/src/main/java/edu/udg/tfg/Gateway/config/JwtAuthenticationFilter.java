package edu.udg.tfg.Gateway.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.http.*;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component
public class JwtAuthenticationFilter implements GatewayFilter {

    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);
    private static final String X_USER_ID_HEADER = "X-User-Id";
    private static final String X_CLIENT_TYPE_HEADER = "X-Client-Type";

    private final WebClient webClient;
    private final ObjectMapper objectMapper;

    @Value("${access.token.name}")
    private String accessTokenName;

    @Value("${auth.service.url}")
    private String authServiceUrl;

    public JwtAuthenticationFilter(@LoadBalanced WebClient.Builder webClientBuilder, @Value("${auth.service.url}") String authServiceUrl, ObjectMapper objectMapper1) {
        this.authServiceUrl = authServiceUrl;
        this.webClient = webClientBuilder.baseUrl("http://UserAuthentication").build();
        this.objectMapper = objectMapper1;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        HttpHeaders headers = request.getHeaders();

        // Check if the Authorization header is present
        if (!headers.containsKey(HttpHeaders.AUTHORIZATION)) {
            logger.warn("Missing Authorization header");
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }

        // Extract the token from the Authorization header
        String authHeader = headers.getFirst(HttpHeaders.AUTHORIZATION);
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            logger.warn("Invalid Authorization header");
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }

        String accessToken = authHeader.substring(7); // Remove "Bearer " prefix

        // Check if the clientType header is present and valid
        if (!headers.containsKey(X_CLIENT_TYPE_HEADER) || headers.get(X_CLIENT_TYPE_HEADER) == null || headers.get(X_CLIENT_TYPE_HEADER).size() != 1) {
            logger.warn("Missing or invalid clientType header");
            exchange.getResponse().setStatusCode(HttpStatus.BAD_REQUEST);
            return exchange.getResponse().setComplete();
        }

        String clientType = headers.getFirst(X_CLIENT_TYPE_HEADER);

        return webClient.method(HttpMethod.POST)
                .uri("/users/auth/check")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken) // Pass the token in the Authorization header
                .retrieve()
                .onStatus(HttpStatusCode::isError, response -> {
                    logger.error("Invalid token response from auth service");
                    return Mono.error(new InvalidTokenException());
                })
                .bodyToMono(String.class)
                .flatMap(userId -> {
                    try {
                        String id = objectMapper.readValue(userId, String.class);
                        ServerHttpRequest modifiedRequest = request.mutate()
                                .header(X_USER_ID_HEADER, id)
                                .header(X_CLIENT_TYPE_HEADER, clientType)
                                .build();
                        return chain.filter(exchange.mutate().request(modifiedRequest).build());
                    } catch (Exception e) {
                        logger.error("Error processing request", e);
                        exchange.getResponse().setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR);
                        return exchange.getResponse().setComplete();
                    }
                })
                .onErrorResume(e -> {
                    logger.error("Error during authentication process", e);
                    exchange.getResponse().setStatusCode(
                            e instanceof InvalidTokenException ? HttpStatus.UNAUTHORIZED : HttpStatus.INTERNAL_SERVER_ERROR
                    );
                    return exchange.getResponse().setComplete();
                });
    }
}
