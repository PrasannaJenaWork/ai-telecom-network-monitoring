package com.telecom.monitoring.backend.alarm.infrastructure.persistence;

import com.telecom.monitoring.backend.alarm.application.port.out.AlarmRepository;
import com.telecom.monitoring.backend.alarm.domain.Alarm;
import org.springframework.stereotype.Repository;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.UnaryOperator;

@Repository
public class InMemoryAlarmRepository implements AlarmRepository {

	private static final Comparator<Alarm> NEWEST_FIRST =
			Comparator.comparing(Alarm::createdAt)
					.reversed()
					.thenComparing(Alarm::id);

	private final ConcurrentMap<UUID, Alarm> alarms = new ConcurrentHashMap<>();

	@Override
	public Alarm save(Alarm alarm) {
		Objects.requireNonNull(alarm, "alarm must not be null");
		Alarm existing = alarms.putIfAbsent(alarm.id(), alarm);
		if (existing != null) {
			throw new IllegalStateException("Alarm %s already exists".formatted(alarm.id()));
		}
		return alarm;
	}

	@Override
	public Optional<Alarm> findById(UUID alarmId) {
		return Optional.ofNullable(alarms.get(alarmId));
	}

	@Override
	public List<Alarm> findAll() {
		return alarms.values().stream()
				.sorted(NEWEST_FIRST)
				.toList();
	}

	@Override
	public Optional<Alarm> updateAtomically(UUID alarmId, UnaryOperator<Alarm> transition) {
		Objects.requireNonNull(alarmId, "alarmId must not be null");
		Objects.requireNonNull(transition, "transition must not be null");

		Alarm updated = alarms.computeIfPresent(
				alarmId,
				(ignored, current) -> Objects.requireNonNull(
						transition.apply(current),
						"transition must not return null"
				)
		);
		return Optional.ofNullable(updated);
	}
}
