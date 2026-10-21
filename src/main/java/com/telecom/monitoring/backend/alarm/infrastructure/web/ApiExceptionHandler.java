package com.telecom.monitoring.backend.alarm.infrastructure.web;

import com.telecom.monitoring.backend.alarm.application.exception.AlarmNotFoundException;
import com.telecom.monitoring.backend.alarm.domain.AlarmAlreadyAcknowledgedException;
import com.telecom.monitoring.backend.alarm.infrastructure.web.dto.ApiErrorResponse;
import com.telecom.monitoring.backend.alarm.infrastructure.web.dto.FieldValidationError;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.time.Clock;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;

@RestControllerAdvice
public class ApiExceptionHandler {

	private final Clock clock;

	public ApiExceptionHandler(Clock clock) {
		this.clock = clock;
	}

	@ExceptionHandler(AlarmNotFoundException.class)
	ResponseEntity<ApiErrorResponse> handleNotFound(
			AlarmNotFoundException exception,
			HttpServletRequest request
	) {
		return error(HttpStatus.NOT_FOUND, exception.getMessage(), request, List.of());
	}

	@ExceptionHandler(AlarmAlreadyAcknowledgedException.class)
	ResponseEntity<ApiErrorResponse> handleAlreadyAcknowledged(
			AlarmAlreadyAcknowledgedException exception,
			HttpServletRequest request
	) {
		return error(HttpStatus.CONFLICT, exception.getMessage(), request, List.of());
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	ResponseEntity<ApiErrorResponse> handleValidation(
			MethodArgumentNotValidException exception,
			HttpServletRequest request
	) {
		List<FieldValidationError> fieldErrors = exception.getBindingResult().getFieldErrors().stream()
				.map(fieldError -> new FieldValidationError(fieldError.getField(), fieldError.getDefaultMessage()))
				.sorted(Comparator.comparing(FieldValidationError::field))
				.toList();

		return error(HttpStatus.BAD_REQUEST, "Request validation failed", request, fieldErrors);
	}

	@ExceptionHandler(HttpMessageNotReadableException.class)
	ResponseEntity<ApiErrorResponse> handleUnreadableMessage(
			HttpMessageNotReadableException exception,
			HttpServletRequest request
	) {
		return error(HttpStatus.BAD_REQUEST, "Request body is malformed or contains an invalid value", request, List.of());
	}

	@ExceptionHandler(MethodArgumentTypeMismatchException.class)
	ResponseEntity<ApiErrorResponse> handleTypeMismatch(
			MethodArgumentTypeMismatchException exception,
			HttpServletRequest request
	) {
		return error(HttpStatus.BAD_REQUEST, "Path parameter has an invalid value", request, List.of());
	}

	private ResponseEntity<ApiErrorResponse> error(
			HttpStatus status,
			String message,
			HttpServletRequest request,
			List<FieldValidationError> fieldErrors
	) {
		ApiErrorResponse response = new ApiErrorResponse(
				Instant.now(clock),
				status.value(),
				status.getReasonPhrase(),
				message,
				request.getRequestURI(),
				fieldErrors
		);
		return ResponseEntity.status(status).body(response);
	}
}
