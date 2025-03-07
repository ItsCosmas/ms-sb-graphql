package dev.cozy.microservices.graphql.service;

import dev.cozy.microservices.graphql.model.User;
import dev.cozy.microservices.graphql.dto.request.UserRequest;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface GraphQLService {

	Mono<String> generateGreeting(String name);

	Mono<User> getUserById(Long id);

	Flux<User> getUsersByName(String name);

	Mono<User> saveUser(UserRequest request);

}
