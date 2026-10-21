package com.telecom.monitoring.backend.alarm.application.port.out;

import com.telecom.monitoring.backend.alarm.domain.Alarm;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.UnaryOperator;

public interface AlarmRepository {

	Alarm save(Alarm alarm);

	Optional<Alarm> findById(UUID alarmId);

	List<Alarm> findAll();

	Optional<Alarm> updateAtomically(UUID alarmId, UnaryOperator<Alarm> transition);
}
