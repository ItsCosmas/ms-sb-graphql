package dev.cozy.microservices.graphql.config;

import org.springframework.graphql.server.WebGraphQlInterceptor;
import org.springframework.graphql.server.WebGraphQlRequest;
import org.springframework.graphql.server.WebGraphQlResponse;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
class ResponseHeaderInterceptor implements WebGraphQlInterceptor {

	private static final String CORRELATION_ID_HEADER = "x-correlation-id";

	@Override
	public @NonNull Mono<WebGraphQlResponse> intercept(@NonNull WebGraphQlRequest request, @NonNull Chain chain) {
		return chain.next(request).doOnNext(response -> {
			// Retrieve correlation ID from GraphQL context
			String correlationId = response.getExecutionInput().getGraphQLContext().get(CORRELATION_ID_HEADER);
			response.getResponseHeaders().add(CORRELATION_ID_HEADER, correlationId);
		});
	}

}