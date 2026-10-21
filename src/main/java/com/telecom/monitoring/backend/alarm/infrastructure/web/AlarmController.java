package com.telecom.monitoring.backend.alarm.infrastructure.web;

import com.telecom.monitoring.backend.alarm.application.port.in.AcknowledgeAlarmUseCase;
import com.telecom.monitoring.backend.alarm.application.port.in.CreateAlarmUseCase;
import com.telecom.monitoring.backend.alarm.application.port.in.GetAlarmUseCase;
import com.telecom.monitoring.backend.alarm.application.port.in.ListAlarmsUseCase;
import com.telecom.monitoring.backend.alarm.domain.Alarm;
import com.telecom.monitoring.backend.alarm.infrastructure.web.dto.AlarmResponse;
import com.telecom.monitoring.backend.alarm.infrastructure.web.dto.CreateAlarmRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/alarms")
public class AlarmController {

	private final CreateAlarmUseCase createAlarmUseCase;
	private final ListAlarmsUseCase listAlarmsUseCase;
	private final GetAlarmUseCase getAlarmUseCase;
	private final AcknowledgeAlarmUseCase acknowledgeAlarmUseCase;

	public AlarmController(
			CreateAlarmUseCase createAlarmUseCase,
			ListAlarmsUseCase listAlarmsUseCase,
			GetAlarmUseCase getAlarmUseCase,
			AcknowledgeAlarmUseCase acknowledgeAlarmUseCase
	) {
		this.createAlarmUseCase = createAlarmUseCase;
		this.listAlarmsUseCase = listAlarmsUseCase;
		this.getAlarmUseCase = getAlarmUseCase;
		this.acknowledgeAlarmUseCase = acknowledgeAlarmUseCase;
	}

	@PostMapping
	public ResponseEntity<AlarmResponse> createAlarm(@Valid @RequestBody CreateAlarmRequest request) {
		Alarm alarm = createAlarmUseCase.createAlarm(request.toCommand());
		URI location = ServletUriComponentsBuilder.fromCurrentRequest()
				.path("/{id}")
				.buildAndExpand(alarm.id())
				.toUri();

		return ResponseEntity.created(location).body(AlarmResponse.from(alarm));
	}

	@GetMapping
	public List<AlarmResponse> listAlarms() {
		return listAlarmsUseCase.listAlarms().stream()
				.map(AlarmResponse::from)
				.toList();
	}

	@GetMapping("/{id}")
	public AlarmResponse getAlarm(@PathVariable UUID id) {
		return AlarmResponse.from(getAlarmUseCase.getAlarm(id));
	}

	@PatchMapping("/{id}/acknowledge")
	public AlarmResponse acknowledgeAlarm(@PathVariable UUID id) {
		return AlarmResponse.from(acknowledgeAlarmUseCase.acknowledgeAlarm(id));
	}
}
