package com.example.weathersensor.controller;

import com.example.weathersensor.dto.SensorReadingRequest;
import com.example.weathersensor.dto.SensorReadingResponse;
import com.example.weathersensor.entity.SensorReading;
import com.example.weathersensor.service.SensorReadingService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.*;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc(addFilters = false)
@WebMvcTest(SensorReadingController.class)
public class SensorReadingControllerTest {
        @Autowired
        private MockMvc mockMvc;

        @MockitoBean
        private SensorReadingService sensorReadingService;

        @Autowired
        private ObjectMapper objectMapper;

        @Test
        public void testRegisterSensorData() throws Exception {
                // Prepare test data
                Instant instant = Instant.now();
                ZonedDateTime targetTime = instant.atZone(ZoneId.of("Asia/Kolkata"));

                SensorReadingRequest request = new SensorReadingRequest("SENSOR_001", Double.valueOf(23.5),
                                Double.valueOf(0), Double.valueOf(0), instant);

                SensorReadingResponse sensorReadingResponse = new SensorReadingResponse(1l, "SENSOR_001",
                                Double.valueOf(23.5), Double.valueOf(0), Double.valueOf(0), targetTime);

                when(sensorReadingService.registerReading(any(SensorReadingRequest.class)))
                                .thenReturn(sensorReadingResponse);

                // Perform POST request
                mockMvc.perform(post("/api/v1/sensor-readings")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isCreated())
                                .andExpect(jsonPath("$.sensorId").value("SENSOR_001"))
                                .andExpect(jsonPath("$.temperature").value(Double.valueOf(23.5)));
        }

}