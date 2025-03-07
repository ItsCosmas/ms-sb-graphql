package dev.cozy.microservices.graphql.exception;

import lombok.Getter;

@Getter
public class CustomGraphQLException extends RuntimeException {

	private final int statusCode;

	public CustomGraphQLException(int statusCode, String message) {
		super(message);
		this.statusCode = statusCode;
	}

}
