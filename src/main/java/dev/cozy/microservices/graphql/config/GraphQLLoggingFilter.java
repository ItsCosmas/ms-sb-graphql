package dev.cozy.microservices.graphql.config;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

@Slf4j
@Component
public class GraphQLLoggingFilter implements WebFilter {
    private static final String CORRELATION_ID_HEADER = "x-correlation-id";
    private static final String GRAPHIQL_PATH = "/graphiql";

    @Override
    public @NonNull Mono<Void> filter(@NonNull ServerWebExchange exchange, @NonNull WebFilterChain chain) {
        String path = exchange.getRequest().getURI().getPath();

        // Skip logging for GraphiQL UI
        if (GRAPHIQL_PATH.equals(path)) {
            return chain.filter(exchange);
        }

        return Mono.deferContextual(ctx -> {
            String correlationId = ctx.get(CORRELATION_ID_HEADER); // Guaranteed to exist

            log.info("RequestID:{} | Path={} | Headers={}",
                    correlationId, exchange.getRequest().getURI().getPath(), exchange.getRequest().getHeaders());

            return chain.filter(exchange)
                    .doOnSuccess(aVoid -> logResponse(exchange, correlationId));
        });
    }

    private void logResponse(ServerWebExchange exchange, String correlationId) {
        ServerHttpResponse response = exchange.getResponse();
        log.info("RequestID:{} | Status={} | Headers={}",
                correlationId, response.getStatusCode(), response.getHeaders());
    }
}
