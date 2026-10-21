package com.telecom.monitoring.backend.alarm.application.service;

import com.telecom.monitoring.backend.alarm.application.exception.AlarmNotFoundException;
import com.telecom.monitoring.backend.alarm.application.port.in.AcknowledgeAlarmUseCase;
import com.telecom.monitoring.backend.alarm.application.port.in.CreateAlarmCommand;
import com.telecom.monitoring.backend.alarm.application.port.in.CreateAlarmUseCase;
import com.telecom.monitoring.backend.alarm.application.port.in.GetAlarmUseCase;
import com.telecom.monitoring.backend.alarm.application.port.in.ListAlarmsUseCase;
import com.telecom.monitoring.backend.alarm.application.port.out.AlarmRepository;
import com.telecom.monitoring.backend.alarm.domain.Alarm;

import java.time.Clock;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public final class AlarmService implements
		CreateAlarmUseCase,
		ListAlarmsUseCase,
		GetAlarmUseCase,
		AcknowledgeAlarmUseCase {

	private final AlarmRepository alarmRepository;
	private final Clock clock;

	public AlarmService(AlarmRepository alarmRepository, Clock clock) {
		this.alarmRepository = Objects.requireNonNull(alarmRepository);
		this.clock = Objects.requireNonNull(clock);
	}

	@Override
	public Alarm createAlarm(CreateAlarmCommand command) {
		Objects.requireNonNull(command, "command must not be null");
		Alarm alarm = Alarm.create(
				UUID.randomUUID(),
				command.nodeId(),
				command.alarmType(),
				command.severity(),
				command.description(),
				clock.instant()
		);
		return alarmRepository.save(alarm);
	}

	@Override
	public List<Alarm> listAlarms() {
		return alarmRepository.findAll();
	}

	@Override
	public Alarm getAlarm(UUID alarmId) {
		return alarmRepository.findById(alarmId)
				.orElseThrow(() -> new AlarmNotFoundException(alarmId));
	}

	@Override
	public Alarm acknowledgeAlarm(UUID alarmId) {
		return alarmRepository.updateAtomically(
				alarmId,
				alarm -> alarm.acknowledge(clock.instant())
		).orElseThrow(() -> new AlarmNotFoundException(alarmId));
	}
}
