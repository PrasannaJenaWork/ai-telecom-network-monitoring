package com.telecom.monitoring.backend.alarm.application.port.in;

import com.telecom.monitoring.backend.alarm.domain.Alarm;

import java.util.UUID;

public interface AcknowledgeAlarmUseCase {

	Alarm acknowledgeAlarm(UUID alarmId);
}
