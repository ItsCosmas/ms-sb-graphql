package dev.cozy.microservices.graphql.exception;

import graphql.GraphQLError;
import graphql.GraphqlErrorBuilder;
import graphql.schema.DataFetchingEnvironment;
import org.springframework.graphql.execution.DataFetcherExceptionResolver;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import org.springframework.lang.NonNull;

import java.util.Collections;
import java.util.List;

@Component
public class GraphQLExceptionHandler implements DataFetcherExceptionResolver {

    @Override
    public @NonNull Mono<List<GraphQLError>> resolveException(Throwable ex, DataFetchingEnvironment env) {
        return Mono.just(
                Collections.singletonList(GraphqlErrorBuilder.newError()
                        .message(ex.getMessage())  // Provide meaningful error message
                        .path(env.getExecutionStepInfo().getPath())  // Trace the request path
                        .location(env.getField().getSourceLocation()) // Show where the error occurred
                        .build())
        );
    }
}
