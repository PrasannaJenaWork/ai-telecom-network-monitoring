package com.telecom.monitoring.backend.alarm.application.port.in;

import com.telecom.monitoring.backend.alarm.domain.Alarm;

import java.util.List;

public interface ListAlarmsUseCase {

	List<Alarm> listAlarms();
}
