package com.telecom.monitoring.backend.alarm.domain;

import java.util.UUID;

public final class AlarmAlreadyAcknowledgedException extends RuntimeException {

	public AlarmAlreadyAcknowledgedException(UUID alarmId) {
		super("Alarm %s has already been acknowledged".formatted(alarmId));
	}
}
