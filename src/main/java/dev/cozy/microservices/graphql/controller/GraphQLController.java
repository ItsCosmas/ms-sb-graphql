package dev.cozy.microservices.graphql.controller;

import dev.cozy.microservices.graphql.model.User;
import dev.cozy.microservices.graphql.dto.request.UserRequest;
import dev.cozy.microservices.graphql.service.GraphQLService;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Controller
public class GraphQLController {
    private final GraphQLService graphQLService;

    public GraphQLController(GraphQLService graphQLService) {
        this.graphQLService = graphQLService;
    }

    @QueryMapping
    public Mono<User> getUserById(@Argument Long id) {
        return graphQLService.getUserById(id);
    }

    @QueryMapping
    public Flux<User> getUsersByName(@Argument String name) {
        return graphQLService.getUsersByName(name);
    }

    @MutationMapping
    public Mono<User> createUser(@Argument UserRequest request) {
        return graphQLService.saveUser(request);
    }

    @QueryMapping
    public Mono<String> greeting(@Argument String name) {
        return graphQLService.generateGreeting(name);
    }
}
