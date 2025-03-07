package dev.cozy.microservices.graphql.config;

import graphql.ExecutionInput;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

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

		return chain.filter(exchange).publishOn(Schedulers.boundedElastic()).doOnEach(signal -> {
			if (signal.isOnNext()) {
				Object executionInputObj = exchange.getAttribute("graphql-execution-input");
				if (executionInputObj instanceof ExecutionInput executionInput) {
					String correlationId = executionInput.getGraphQLContext().get(CORRELATION_ID_HEADER);
					log.info("RequestID:{} | Path={} | Headers={}", correlationId,
							exchange.getRequest().getURI().getPath(), exchange.getRequest().getHeaders());
				}
			}
		}).doOnSuccess(aVoid -> logResponse(exchange));
	}

	private void logResponse(ServerWebExchange exchange) {
		ServerHttpResponse response = exchange.getResponse();
		log.info("Status={} | Headers={}", response.getStatusCode(), response.getHeaders());
	}

}
