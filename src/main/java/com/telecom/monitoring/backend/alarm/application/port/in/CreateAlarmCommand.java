package com.telecom.monitoring.backend.alarm.application.port.in;

import com.telecom.monitoring.backend.alarm.domain.AlarmSeverity;

public record CreateAlarmCommand(
		String nodeId,
		String alarmType,
		AlarmSeverity severity,
		String description
) {
}
