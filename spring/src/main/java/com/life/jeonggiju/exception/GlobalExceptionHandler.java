package com.life.jeonggiju.exception;


import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.life.jeonggiju.domain.user.exception.UserException;
import com.life.jeonggiju.domain.user.exception.UserNotFoundException;

import jakarta.servlet.http.HttpServletRequest;

@RestControllerAdvice
public class GlobalExceptionHandler {



	@ExceptionHandler(UserException.class)
	public ResponseEntity<ErrorResponse> handleUserException(UserNotFoundException ex){
		ErrorResponse response = new ErrorResponse(ex);
		return ResponseEntity
			.status(ex.getErrorCode().getHttpStatus())
			.body(response);
	}

	@ExceptionHandler(BaseException.class)
	public ResponseEntity<ErrorResponse> handleBaseException(BaseException ex) {
		ErrorResponse response = new ErrorResponse(ex);
		return ResponseEntity
				.status(ex.getErrorCode().getHttpStatus())
				.body(response);
	}

	@ExceptionHandler(Exception.class)
	public ResponseEntity<ErrorResponse> handleAllExceptions(
		Exception ex,
		HttpServletRequest request
	) {
		if (request.getHeader("Accept") != null &&
			request.getHeader("Accept").contains("text/event-stream")) {
			return null;
		}

		ErrorResponse response = new ErrorResponse(ex, HttpStatus.INTERNAL_SERVER_ERROR.value());
		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
	}
}
