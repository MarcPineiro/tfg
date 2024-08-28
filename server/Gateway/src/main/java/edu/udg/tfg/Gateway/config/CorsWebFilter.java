package edu.udg.tfg.Gateway.config;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class CorsWebFilter implements WebFilter {

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        // Add X-Client-Type header based on User-Agent
        String userAgent = exchange.getRequest().getHeaders().getFirst("User-Agent");
        if (userAgent != null) {
            if (userAgent.contains("Mozilla") || userAgent.contains("Chrome") || userAgent.contains("Safari")) {
                exchange.getResponse().getHeaders().add("X-Client-Type", "web");
            } else {
                exchange.getResponse().getHeaders().add("X-Client-Type", "desktop");
            }
        }

        if (exchange.getRequest().getMethod() == HttpMethod.OPTIONS) {
            exchange.getResponse().getHeaders().add("Access-Control-Allow-Origin", "*");
            exchange.getResponse().getHeaders().add("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
            exchange.getResponse().getHeaders().add("Access-Control-Allow-Headers", "*");
            exchange.getResponse().getHeaders().add("Access-Control-Expose-Headers", "Authorization, X-Refresh-Token");
            exchange.getResponse().getHeaders().add("Access-Control-Allow-Credentials", "true");
            exchange.getResponse().setStatusCode(HttpStatus.OK);
            return exchange.getResponse().setComplete();
        }
        return chain.filter(exchange);
    }
}