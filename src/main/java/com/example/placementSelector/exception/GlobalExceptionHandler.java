package com.example.placementSelector.exception;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;

/**
 * Global exception handler for REST API errors.
 *
 * <p>Converts exceptions into a consistent JSON error response format
 * for all API endpoints.</p>
 */
@RestControllerAdvice
public class GlobalExceptionHandler {
	
	/**
	 * Handles application-defined HTTP exceptions.
	 *
	 * @param ex the thrown ResponseStatusException
	 * @return structured error response with status and message
	 */
	@ExceptionHandler(ResponseStatusException.class)
	public ResponseEntity<ErrorResponse> handle(ResponseStatusException ex) {
		
		ErrorResponse body = new ErrorResponse(
				ex.getReason(),
				ex.getStatusCode().value(),
				Instant.now()
		);
		
		return ResponseEntity
					   .status(ex.getStatusCode())
					   .body(body);
	}
	
	/**
	 * Handles unexpected internal server errors.
	 *
	 * <p>Ensures the API does not expose stack traces or internal implementation details.</p>
	 *
	 * @param ex unexpected exception
	 * @return generic 500 error response
	 */
	@ExceptionHandler(Exception.class)
	public ResponseEntity<ErrorResponse> handleGeneric(Exception ex) {
		
		ErrorResponse body = new ErrorResponse(
				"Internal server error",
				500,
				Instant.now()
		);
		
		return ResponseEntity.status(500).body(body);
	}
}
