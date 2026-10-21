package com.telecom.monitoring.backend.alarm.application.port.in;

import com.telecom.monitoring.backend.alarm.domain.Alarm;

public interface CreateAlarmUseCase {

	Alarm createAlarm(CreateAlarmCommand command);
}
