package com.example.weathersensor.integration;

import com.example.weathersensor.WeatherSensorApiApplication;
import com.example.weathersensor.dto.SensorReadingRequest;
import com.example.weathersensor.dto.SensorRegistrationRequest;
import com.example.weathersensor.entity.Sensor;
import com.example.weathersensor.entity.SensorReading;
import com.example.weathersensor.repository.SensorReadingRepository;
import com.example.weathersensor.repository.SensorRepository;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.*;

@SpringBootTest(classes = WeatherSensorApiApplication.class)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
@ActiveProfiles("test")
public class SensorReadingControllerIntegrationTest {

        @Autowired
        private WebApplicationContext webApplicationContext;
        @Autowired
        private SensorReadingRepository repository;
        @Autowired
        private SensorRepository sensorRepository;
        @Autowired
        private ObjectMapper objectMapper;

        private MockMvc mockMvc;

        @BeforeEach
        public void setup() {
                mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
                repository.deleteAll();
                sensorRepository.deleteAll();
                sensorRepository.saveAll(List.of(
                                new Sensor("SENSOR_001", "Berlin", "Europe/Berlin"),
                                new Sensor("SENSOR_002", "Berlin", "Europe/Berlin"),
                                new Sensor("SENSOR_003", "Delhi", "Asia/Kolkata")));
        }

        private void postReading(SensorReadingRequest request) throws Exception {
                mockMvc.perform(post("/api/v1/sensor-readings")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isCreated());
        }

        private List<SensorReadingRequest> createReadings(Instant baseInstant) {
                return List.of(
                                new SensorReadingRequest("SENSOR_001", 23.5, 0.0, 0.0, baseInstant),
                                new SensorReadingRequest("SENSOR_001", 23.5, 0.0, 0.0,
                                                baseInstant.plus(1, ChronoUnit.HOURS)),
                                new SensorReadingRequest("SENSOR_002", 23.5, 0.0, 0.0, baseInstant),
                                new SensorReadingRequest("SENSOR_003", 23.5, 0.0, 0.0, baseInstant));
        }

        @Test
        public void testSensorReadingCreation() throws Exception {
                Instant now = Instant.now().truncatedTo(ChronoUnit.SECONDS);
                for (SensorReadingRequest req : createReadings(now).subList(0, 3)) {
                        postReading(req);
                }
                assertEquals(3, repository.count());
        }

        @Test
        public void test400ErrorForSensorReadingCreation() throws Exception {
                var invalidPayload = new SensorRegistrationRequest("SENSOR_001", "Berlin", "Europe/Berlin");
                mockMvc.perform(post("/api/v1/sensor-readings")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(invalidPayload)))
                                .andExpect(status().isBadRequest());
        }

        @Test
        public void test422UnprocessableEntity() throws Exception {
                SensorReadingRequest req = new SensorReadingRequest("NO_SENSOR", 23.5, 0.0, 0.0, Instant.now());
                mockMvc.perform(post("/api/v1/sensor-readings")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(req)))
                                .andExpect(status().isUnprocessableEntity());
        }

        @Test
        public void testNoMetricsFoundInTimeFrame() throws Exception {
                mockMvc.perform(get("/api/v1/sensor-readings/average")
                                .param("startTime", "2024-07-01T10:00:00Z")
                                .param("endTime", "2024-07-01T10:00:00Z"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.readings").value(0))
                                .andExpect(jsonPath("$.averageTemperature").doesNotExist())
                                .andExpect(jsonPath("$.averageHumidity").doesNotExist())
                                .andExpect(jsonPath("$.averageWindSpeed").doesNotExist());
        }

        @Test
        public void testInternalServerError500() throws Exception {
                mockMvc.perform(get("/api/v1/sensor-readings/average")
                                .param("startDate", "2024-07-01T10:00:00Z") // Wrong param
                                .param("endTime", "2024-07-01T10:00:00Z"))
                                .andExpect(status().isInternalServerError())
                                .andExpect(jsonPath("$.message").exists())
                                .andExpect(jsonPath("$.status").value(500))
                                .andExpect(jsonPath("$.path").value("/api/v1/sensor-readings/average"))
                                .andExpect(jsonPath("$.timestamp").exists());
        }

        @Test
        public void testNoMetricsFoundForSpecificSensor() throws Exception {
                mockMvc.perform(get("/api/v1/sensor-readings/average/sensorId")
                                .param("startTime", "2024-07-01T10:00:00Z")
                                .param("endTime", "2024-07-01T10:00:00Z"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.readings").value(0))
                                .andExpect(jsonPath("$.averageTemperature").doesNotExist())
                                .andExpect(jsonPath("$.averageHumidity").doesNotExist())
                                .andExpect(jsonPath("$.averageWindSpeed").doesNotExist());
        }

        @Test
        public void testMetricsFoundForSpecificSensorInTimeFrame() throws Exception {
                Instant now = Instant.now().truncatedTo(ChronoUnit.SECONDS);
                for (SensorReadingRequest req : createReadings(now)) {
                        if (!req.sensorId().equals("SENSOR_003")) {
                                postReading(req);
                        }
                }

                Instant start = now.minus(30, ChronoUnit.MINUTES);
                Instant end = now.plus(2, ChronoUnit.HOURS);

                mockMvc.perform(get("/api/v1/sensor-readings/average/SENSOR_001")
                                .param("startTime", DateTimeFormatter.ISO_INSTANT.format(start))
                                .param("endTime", DateTimeFormatter.ISO_INSTANT.format(end)))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.readings").value(2))
                                .andExpect(jsonPath("$.averageTemperature").exists())
                                .andExpect(jsonPath("$.averageHumidity").exists())
                                .andExpect(jsonPath("$.averageWindSpeed").exists());
        }

        @Test
        public void testAllSensorReadingsInTimeFrame() throws Exception {
                Instant instant = Instant.now().truncatedTo(ChronoUnit.SECONDS);
                for (SensorReadingRequest req : createReadings(instant)) {
                        postReading(req);
                }

                // Expected timestamps
                DateTimeFormatter formatter = DateTimeFormatter.ISO_OFFSET_DATE_TIME;
                String ts1 = formatter.format(instant.atZone(ZoneId.of("Europe/Berlin")));
                String ts2 = formatter.format(instant.plus(1, ChronoUnit.HOURS).atZone(ZoneId.of("Europe/Berlin")));
                String ts3 = formatter.format(instant.atZone(ZoneId.of("Asia/Kolkata")));

                Instant start = instant.minus(30, ChronoUnit.MINUTES);
                Instant end = instant.plus(2, ChronoUnit.HOURS);

                mockMvc.perform(get("/api/v1/sensor-readings")
                                .param("startTime", DateTimeFormatter.ISO_INSTANT.format(start))
                                .param("endTime", DateTimeFormatter.ISO_INSTANT.format(end)))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.length()").value(4))
                                .andExpect(jsonPath("$[*].sensorId")
                                                .value(hasItems("SENSOR_001", "SENSOR_002", "SENSOR_003")))
                                .andExpect(jsonPath("$[*].temperature").value(everyItem(is(23.5))))
                                .andExpect(jsonPath("$[*].humidity").value(everyItem(is(0.0))))
                                .andExpect(jsonPath("$[*].windSpeed").value(everyItem(is(0.0))))
                                .andExpect(jsonPath("$[*].timestamp").value(hasItems(ts1, ts2, ts3)));
        }
}
