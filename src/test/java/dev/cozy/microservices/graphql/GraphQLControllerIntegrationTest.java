package dev.cozy.microservices.graphql;

import dev.cozy.microservices.graphql.model.User;
import dev.cozy.microservices.graphql.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.graphql.tester.AutoConfigureGraphQlTester;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.graphql.test.tester.GraphQlTester;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureGraphQlTester
class GraphQLControllerIntegrationTest {

	@Autowired
	private GraphQlTester graphQlTester;

	@Autowired
	private UserRepository userRepository;

	@BeforeEach
	void setUp() {
		userRepository.deleteAll().block();
	}

	@Test
	void getUserById_ReturnsUser() {
		User savedUser = userRepository.save(new User(null, "John Doe", "john@example.com")).block();
		assert savedUser != null;

		graphQlTester.documentName("getUserById") // Uses getUserById.graphql
			.variable("id", savedUser.getId()) // Passes ID as a variable
			.execute()
			.path("data.getUserById")
			.entity(User.class)
			.satisfies(user -> {
				assertThat(user.getId()).isEqualTo(savedUser.getId());
				assertThat(user.getName()).isEqualTo("John Doe");
				assertThat(user.getEmail()).isEqualTo("john@example.com");
			});
	}

	@Test
	void createUser_ReturnsSavedUser() {
		graphQlTester.documentName("createUser") // Uses createUser.graphql
			.variable("request", Map.of("name", "Alice", "email", "alice@example.com")) // Pass
																						// variables
			.execute()
			.path("data.createUser")
			.entity(User.class)
			.satisfies(user -> {
				assertThat(user.getName()).isEqualTo("Alice");
				assertThat(user.getEmail()).isEqualTo("alice@example.com");
			});
	}

	@Test
	void greeting_ReturnsGreetingMessage() {
		graphQlTester.documentName("greeting") // Uses greeting.graphql
			.variable("name", "Alice") // Passes "Alice" as a variable
			.execute()
			.path("data.greeting")
			.entity(String.class)
			.isEqualTo("Hello, Alice! Welcome to GraphQL.");
	}

}
