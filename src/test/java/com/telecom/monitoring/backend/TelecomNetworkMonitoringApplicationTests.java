package com.telecom.monitoring.backend;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
class TelecomNetworkMonitoringApplicationTests {

	@Autowired
	private ApplicationContext applicationContext;

	@Test
	void applicationStartsWithExpectedConfiguration() {
		assertNotNull(applicationContext);
		assertEquals(
				"telecom-network-monitoring",
				applicationContext.getEnvironment().getProperty("spring.application.name")
		);
	}

}
