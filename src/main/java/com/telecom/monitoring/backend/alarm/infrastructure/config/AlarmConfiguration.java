package com.telecom.monitoring.backend.alarm.infrastructure.config;

import com.telecom.monitoring.backend.alarm.application.port.out.AlarmRepository;
import com.telecom.monitoring.backend.alarm.application.service.AlarmService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;

@Configuration(proxyBeanMethods = false)
public class AlarmConfiguration {

	@Bean
	Clock alarmClock() {
		return Clock.systemUTC();
	}

	@Bean
	AlarmService alarmService(AlarmRepository alarmRepository, Clock alarmClock) {
		return new AlarmService(alarmRepository, alarmClock);
	}
}
