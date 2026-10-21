package com.telecom.monitoring.backend.alarm.infrastructure.web;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.net.URI;
import java.util.UUID;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.matchesPattern;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class AlarmControllerMockMvcTest {

	@Autowired
	private MockMvc mockMvc;

	@Test
	void supportsTheCompleteAlarmLifecycle() throws Exception {
		MvcResult createResult = mockMvc.perform(post("/api/v1/alarms")
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
						{
						  "nodeId": "RAN-SE-001",
						  "alarmType": "HIGH_PACKET_LOSS",
						  "severity": "MAJOR",
						  "description": "Packet loss exceeded 10 percent"
						}
						"""))
				.andExpect(status().isCreated())
				.andExpect(header().string(
						"Location",
						matchesPattern("http://localhost/api/v1/alarms/[0-9a-f-]{36}")
				))
				.andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
				.andExpect(jsonPath("$.nodeId").value("RAN-SE-001"))
				.andExpect(jsonPath("$.status").value("ACTIVE"))
				.andExpect(jsonPath("$.acknowledgedAt").doesNotExist())
				.andReturn();

		String location = createResult.getResponse().getHeader("Location");
		String alarmPath = URI.create(location).getPath();
		String alarmId = alarmPath.substring(alarmPath.lastIndexOf('/') + 1);

		mockMvc.perform(get(alarmPath))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id").value(alarmId));

		mockMvc.perform(get("/api/v1/alarms"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$[0].id").value(alarmId));

		mockMvc.perform(patch(alarmPath + "/acknowledge"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.status").value("ACKNOWLEDGED"))
				.andExpect(jsonPath("$.acknowledgedAt").isNotEmpty());

		mockMvc.perform(patch(alarmPath + "/acknowledge"))
				.andExpect(status().isConflict())
				.andExpect(jsonPath("$.status").value(409))
				.andExpect(jsonPath("$.message", containsString("already been acknowledged")));
	}

	@Test
	void rejectsInvalidCreateRequests() throws Exception {
		mockMvc.perform(post("/api/v1/alarms")
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
						{
						  "nodeId": "",
						  "alarmType": "",
						  "description": ""
						}
						"""))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.status").value(400))
				.andExpect(jsonPath("$.message").value("Request validation failed"))
				.andExpect(jsonPath("$.fieldErrors.length()").value(4));

		mockMvc.perform(post("/api/v1/alarms")
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
						{
						  "nodeId": "RAN-SE-001",
						  "alarmType": "HIGH_PACKET_LOSS",
						  "severity": "EMERGENCY",
						  "description": "Packet loss exceeded threshold"
						}
						"""))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.message").value("Request body is malformed or contains an invalid value"));
	}

	@Test
	void reportsMissingAndMalformedAlarmIds() throws Exception {
		UUID missingId = UUID.fromString("00000000-0000-0000-0000-000000000099");

		mockMvc.perform(get("/api/v1/alarms/{id}", missingId))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.status").value(404))
				.andExpect(jsonPath("$.path").value("/api/v1/alarms/" + missingId));

		mockMvc.perform(patch("/api/v1/alarms/{id}/acknowledge", missingId))
				.andExpect(status().isNotFound());

		mockMvc.perform(get("/api/v1/alarms/not-a-uuid"))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.message").value("Path parameter has an invalid value"));
	}
}
