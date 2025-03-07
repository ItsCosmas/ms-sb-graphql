package dev.cozy.microservices.graphql.exception;

import graphql.GraphQLError;
import graphql.GraphqlErrorBuilder;
import graphql.schema.DataFetchingEnvironment;
import org.springframework.graphql.execution.DataFetcherExceptionResolverAdapter;
import org.springframework.graphql.execution.ErrorType;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

import java.util.Collections;

@Component
public class GraphQLExceptionHandler extends DataFetcherExceptionResolverAdapter {

	private static final String MISSING_CORRELATION_ID_FLAG = "missing_correlation_id";

	@Override
	protected GraphQLError resolveToSingleError(@NonNull Throwable ex, @NonNull DataFetchingEnvironment env) {
		// A hacky way to handle missing correlation ID
		// Check if the GraphQL context contains the missing correlation ID flag
		Boolean missingCorrelationId = env.getGraphQlContext().getOrDefault(MISSING_CORRELATION_ID_FLAG, false);

		if (Boolean.TRUE.equals(missingCorrelationId)) {
			return GraphqlErrorBuilder.newError()
				.message("Missing required header: x-correlation-id")
				.extensions(Collections.singletonMap("classification", "BAD_REQUEST"))
				.build();
		}

		if (ex instanceof CustomGraphQLException customEx) {
			ErrorType errorType = switch (customEx.getStatusCode()) {
				case 400 -> ErrorType.BAD_REQUEST;
				case 404 -> ErrorType.NOT_FOUND;
				default -> ErrorType.INTERNAL_ERROR;
			};
			return graphQLError(errorType, customEx, env);
		}

		return GraphqlErrorBuilder.newError()
			.message("An unexpected error occurred")
			.errorType(ErrorType.INTERNAL_ERROR)
			.build();
	}

	private GraphQLError graphQLError(ErrorType errorType, CustomGraphQLException ex, DataFetchingEnvironment env) {
		return GraphqlErrorBuilder.newError()
			.errorType(errorType)
			.message(ex.getMessage())
			.path(env.getExecutionStepInfo().getPath())
			.location(env.getField().getSourceLocation())
			.build();
	}

}
