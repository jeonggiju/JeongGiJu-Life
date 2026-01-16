package com.life.jeonggiju.security.authentication.jwt.exception;

import org.springframework.security.core.AuthenticationException;

public class InValidAccessTokenException extends AuthenticationException {
	public InValidAccessTokenException(String message) {
		super(message);
	}
}

