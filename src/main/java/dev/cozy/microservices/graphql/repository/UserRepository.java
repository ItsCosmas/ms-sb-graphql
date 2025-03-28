package dev.cozy.microservices.graphql.repository;

import dev.cozy.microservices.graphql.model.User;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import reactor.core.publisher.Flux;

public interface UserRepository extends R2dbcRepository<User, Long> {

	Flux<User> findByName(String name);

}
