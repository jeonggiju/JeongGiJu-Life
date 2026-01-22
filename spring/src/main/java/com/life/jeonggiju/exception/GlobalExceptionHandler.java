package com.life.jeonggiju.exception;

import java.time.Instant;
import java.util.Map;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import jakarta.servlet.http.HttpServletRequest;

@RestControllerAdvice
public class GlobalExceptionHandler {

	@ExceptionHandler(BaseException.class)
	public ResponseEntity<ErrorResponse> handleBaseException(BaseException ex) {
		ErrorResponse response = new ErrorResponse(ex);
		return ResponseEntity
			.status(ex.getErrorCode().getHttpStatus())
			.body(response);
	}

	@ExceptionHandler(DataIntegrityViolationException.class)
	public ResponseEntity<ErrorResponse> handleDataIntegrityViolation(DataIntegrityViolationException ex) {
		String message = ex.getMessage();
		ErrorCode errorCode = ErrorCode.DUPLICATE_EMAIL;

		if (message != null && message.toLowerCase().contains("email")) {
			errorCode = ErrorCode.DUPLICATE_EMAIL;
		}

		ErrorResponse response = new ErrorResponse(
			Instant.now(),
			errorCode.name(),
			errorCode.getMessage(),
			Map.of(),
			ex.getClass().getSimpleName(),
			errorCode.getHttpStatus().value()
		);

		return ResponseEntity.status(errorCode.getHttpStatus()).body(response);
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
