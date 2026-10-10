package com.telecom.monitoring.backend.alarm.application.exception;

import java.util.UUID;

public final class AlarmNotFoundException extends RuntimeException {

	public AlarmNotFoundException(UUID alarmId) {
		super("Alarm %s was not found".formatted(alarmId));
	}
}
