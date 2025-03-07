package dev.cozy.microservices.graphql.service.impl;

import dev.cozy.microservices.graphql.model.User;
import dev.cozy.microservices.graphql.repository.UserRepository;
import dev.cozy.microservices.graphql.dto.request.UserRequest;
import dev.cozy.microservices.graphql.service.GraphQLService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Slf4j
@Service
public class GraphQLServiceImpl implements GraphQLService {

	private static final String CORRELATION_ID_HEADER = "x-correlation-id";

	private final UserRepository userRepository;

	public GraphQLServiceImpl(UserRepository userRepository) {
		this.userRepository = userRepository;
	}

	private Mono<String> getCorrelationId() {
		return Mono.deferContextual(ctx -> {
			String correlationId = ctx.getOrDefault(CORRELATION_ID_HEADER, "UNKNOWN");
			return Mono.just(correlationId != null ? correlationId : "UNKNOWN");
		});
	}

	@Override
	public Mono<String> generateGreeting(String name) {
		return getCorrelationId().flatMap(correlationId -> {
			log.info("Processing request | RequestID:{} | Name:{}", correlationId, name);
			String message = "Hello, " + name + "! Welcome to GraphQL.";
			log.info("Response generated | RequestID:{} | Response:{}", correlationId, message);
			return Mono.just(message);
		});
	}

	@Override
	public Mono<User> getUserById(Long id) {
		return getCorrelationId().flatMap(correlationId -> userRepository.findById(id)
			.doOnNext(user -> log.info("Fetched User By ID: {} | RequestID:{}", user, correlationId))
			.switchIfEmpty(Mono.error(new RuntimeException("User not found | RequestID:" + correlationId))));
	}

	@Override
	public Flux<User> getUsersByName(String name) {
		return getCorrelationId().flatMapMany(correlationId -> userRepository.findByName(name)
			.doOnNext(user -> log.info("Fetched User By Name: {} | RequestID:{}", user, correlationId)));
	}

	@Override
	public Mono<User> saveUser(UserRequest request) {
		return userRepository.save(User.builder().name(request.getName()).email(request.getEmail()).build())
			.doOnNext(savedUser -> log.info("Saved User: {}", savedUser));
	}

}
