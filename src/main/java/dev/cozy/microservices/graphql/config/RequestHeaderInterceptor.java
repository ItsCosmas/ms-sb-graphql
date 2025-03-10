package dev.cozy.microservices.graphql.config;

import org.springframework.graphql.server.WebGraphQlInterceptor;
import org.springframework.graphql.server.WebGraphQlRequest;
import org.springframework.graphql.server.WebGraphQlResponse;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.Collections;

@Component
public class RequestHeaderInterceptor implements WebGraphQlInterceptor {

	private static final String CORRELATION_ID_HEADER = "x-correlation-id";

	@Override
	public @NonNull Mono<WebGraphQlResponse> intercept(@NonNull WebGraphQlRequest request, @NonNull Chain chain) {
		String correlationId = request.getHeaders().getFirst(CORRELATION_ID_HEADER);

		if (correlationId == null || correlationId.isBlank()) {
			// Instead of throwing an error, add a flag to the GraphQL context
			request.configureExecutionInput((executionInput, builder) -> builder
				.graphQLContext(Collections.singletonMap("missing_correlation_id", true))
				.build());
		}
		else {
			// Store correlation ID in GraphQL context
			request.configureExecutionInput((executionInput, builder) -> builder
				.graphQLContext(Collections.singletonMap(CORRELATION_ID_HEADER, correlationId))
				.build());
		}

		return chain.next(request);
	}

}
