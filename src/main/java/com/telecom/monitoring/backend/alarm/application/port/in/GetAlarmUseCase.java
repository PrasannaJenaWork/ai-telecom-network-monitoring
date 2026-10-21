package com.telecom.monitoring.backend.alarm.application.port.in;

import com.telecom.monitoring.backend.alarm.domain.Alarm;

import java.util.UUID;

public interface GetAlarmUseCase {

	Alarm getAlarm(UUID alarmId);
}
