package dev.cozy.microservices.graphql.service;

import dev.cozy.microservices.graphql.model.User;
import dev.cozy.microservices.graphql.repository.UserRepository;
import dev.cozy.microservices.graphql.dto.request.UserRequest;
import dev.cozy.microservices.graphql.service.impl.GraphQLServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GraphQLServiceTest {

	@Mock
	private UserRepository userRepository;

	@InjectMocks
	private GraphQLServiceImpl graphQLService;

	private User user;

	@BeforeEach
	void setUp() {
		user = new User(1L, "John Doe", "john.doe@example.com");
	}

	@Test
	void getUserById_ShouldReturnUser() {
		when(userRepository.findById(1L)).thenReturn(Mono.just(user));

		StepVerifier.create(graphQLService.getUserById(1L)).expectNext(user).verifyComplete();

		verify(userRepository, times(1)).findById(1L);
	}

	@Test
	void getUsersByName_ShouldReturnUsers() {
		when(userRepository.findByName("John Doe")).thenReturn(Flux.just(user));

		StepVerifier.create(graphQLService.getUsersByName("John Doe")).expectNext(user).verifyComplete();

		verify(userRepository, times(1)).findByName("John Doe");
	}

	@Test
	void saveUser_ShouldSaveAndReturnUser() {
		UserRequest request = new UserRequest("John Doe", "john.doe@example.com");
		when(userRepository.save(any(User.class))).thenReturn(Mono.just(user));

		StepVerifier.create(graphQLService.saveUser(request)).expectNext(user).verifyComplete();

		verify(userRepository, times(1)).save(any(User.class));
	}

	@Test
	void generateGreeting_ShouldReturnGreetingMessage() {
		StepVerifier.create(graphQLService.generateGreeting("John"))
			.expectNext("Hello, John! Welcome to GraphQL.")
			.verifyComplete();
	}

}
