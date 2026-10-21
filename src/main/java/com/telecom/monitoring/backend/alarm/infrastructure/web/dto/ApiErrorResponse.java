package com.telecom.monitoring.backend.alarm.infrastructure.web.dto;

import java.time.Instant;
import java.util.List;

public record ApiErrorResponse(
		Instant timestamp,
		int status,
		String error,
		String message,
		String path,
		List<FieldValidationError> fieldErrors
) {
}
