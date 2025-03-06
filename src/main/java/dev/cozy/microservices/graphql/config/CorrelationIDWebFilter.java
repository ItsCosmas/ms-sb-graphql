package dev.cozy.microservices.graphql.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;

@Slf4j
@Component
@Order(Ordered.HIGHEST_PRECEDENCE) // Ensures this filter runs first
public class CorrelationIDWebFilter implements WebFilter {
    private static final String CORRELATION_ID_HEADER = "x-correlation-id";
    private static final String ERROR_RESPONSE = """
            {
              "errors": [
                {
                  "message": "Missing required header: x-correlation-id",
                  "extensions": {
                    "classification": "ValidationError"
                  }
                }
              ]
            }
            """;

    @Override
    public @NonNull Mono<Void> filter(@NonNull ServerWebExchange exchange, @NonNull WebFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getURI().getPath();

        // Allow GraphiQL UI requests to proceed without requiring x-correlation-id
        if (path.startsWith("/graphiql")) {
            return chain.filter(exchange);
        }

        String correlationId = request.getHeaders().getFirst(CORRELATION_ID_HEADER);

        if (correlationId == null || correlationId.isBlank()) {
            log.warn("Missing required header: {}", CORRELATION_ID_HEADER);
            return Mono.defer(() -> writeGraphQLErrorResponse(exchange)); // Lazy execution
        }

        log.info("Incoming Request | RequestID:{} | Path:{}", correlationId, path);

        return chain.filter(exchange)
                .contextWrite(ctx -> ctx.put(CORRELATION_ID_HEADER, correlationId))
                .doOnSuccess(aVoid -> log.info("Outgoing Response | RequestID:{}", correlationId));
    }

    private Mono<Void> writeGraphQLErrorResponse(ServerWebExchange exchange) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(HttpStatus.BAD_REQUEST);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);

        return response.writeWith(
                Mono.just(response.bufferFactory().wrap(ERROR_RESPONSE.getBytes(StandardCharsets.UTF_8)))
        );
    }
}
