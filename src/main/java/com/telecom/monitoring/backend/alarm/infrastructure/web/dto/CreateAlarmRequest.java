package com.telecom.monitoring.backend.alarm.infrastructure.web.dto;

import com.telecom.monitoring.backend.alarm.application.port.in.CreateAlarmCommand;
import com.telecom.monitoring.backend.alarm.domain.AlarmSeverity;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateAlarmRequest(
		@NotBlank @Size(max = 100) String nodeId,
		@NotBlank @Size(max = 100) String alarmType,
		@NotNull AlarmSeverity severity,
		@NotBlank @Size(max = 1000) String description
) {

	public CreateAlarmCommand toCommand() {
		return new CreateAlarmCommand(nodeId, alarmType, severity, description);
	}
}
