package com.example.weathersensor.integration;

import com.example.weathersensor.WeatherSensorApiApplication;
import com.example.weathersensor.dto.SensorReadingRequest;
import com.example.weathersensor.dto.SensorReadingResponse;
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
import java.time.temporal.TemporalUnit;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(classes = WeatherSensorApiApplication.class)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
@ActiveProfiles("test")
public class SensorDataIntegrationTest {

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
        repository.deleteAll(); // Clean database before each test
        sensorRepository.deleteAll();
        //Register Sensor
        Sensor sensor = new Sensor("SENSOR_001","Berlin","Europe/Berlin");
        Sensor sensor2 = new Sensor("SENSOR_002","Berlin","Europe/Berlin");
        sensorRepository.saveAll(Arrays.asList(sensor,sensor2));
    }

    @Test
    public void testSensorReadingCreation() throws Exception {

        Instant instant = Instant.now();
        // 1. Register sensor data
        SensorReadingRequest request = new SensorReadingRequest("SENSOR_001", Double.valueOf(23.5), Double.valueOf(0), Double.valueOf(0), instant);
        SensorReadingRequest request2 = new SensorReadingRequest("SENSOR_001", Double.valueOf(23.5), Double.valueOf(0), Double.valueOf(0), instant.plus(1, ChronoUnit.HOURS));
        SensorReadingRequest request3 = new SensorReadingRequest("SENSOR_002", Double.valueOf(23.5), Double.valueOf(0), Double.valueOf(0), instant);


        // Register all data points
        mockMvc.perform(post("/api/v1/sensor-readings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/v1/sensor-readings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request2)))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/v1/sensor-readings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request3)))
                .andExpect(status().isCreated());

        // 2. Verify data is stored
        assertEquals(3, repository.count());

        // 3. Test average for all sensors
//        mockMvc.perform(get("/api/sensor-data/average/temperature")
//                        .param("startDate", "2024-01-01T00:00:00")
//                        .param("endDate", "2024-01-31T23:59:59"))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.metric").value("temperature"))
//                .andExpect(jsonPath("$.averageValue").value(22.833333333333332)) // (23.5 + 25.0 + 20.0) / 3
//                .andExpect(jsonPath("$.dataPointCount").value(3));
//
//        // 4. Test average for specific sensor
//        mockMvc.perform(get("/api/sensor-data/sensor/SENSOR_001/average/temperature")
//                        .param("startDate", "2024-01-01T00:00:00")
//                        .param("endDate", "2024-01-31T23:59:59"))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.metric").value("temperature"))
//                .andExpect(jsonPath("$.averageValue").value(24.25)) // (23.5 + 25.0) / 2
//                .andExpect(jsonPath("$.sensorId").value("SENSOR_001"))
//                .andExpect(jsonPath("$.dataPointCount").value(2));
    }

    @Test
    public void test422UnprocessableEntity() throws Exception {

        // 1. Register sensor data
        SensorReadingRequest request = new SensorReadingRequest("SENSOR_003", Double.valueOf(23.5), Double.valueOf(0), Double.valueOf(0), Instant.now());

        // Register data points with a no existing sensor
        mockMvc.perform(post("/api/v1/sensor-readings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnprocessableEntity());


    }

    @Test
    public void testNoMetricsFoundInTimeFrame() throws Exception {

        // Query empty readings
        mockMvc.perform(get("/api/v1/sensor-readings/average")
                        .contentType(MediaType.APPLICATION_JSON)
                        .param("startTime", "2024-07-01T10:00:00Z")
                        .param("endTime", "2024-07-01T10:00:00Z"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.readings").value(0))// Assert that the readings is 0
                .andExpect(jsonPath("$.averageTemperature").doesNotExist()) // Or .value(null) depending on serialization
                .andExpect(jsonPath("$.averageHumidity").doesNotExist())
                .andExpect(jsonPath("$.averageWindSpeed").doesNotExist());

    }

    @Test
    public void testInternalServerError500() throws Exception {
        // Query empty readings
        mockMvc.perform(get("/api/v1/sensor-readings/average")
                        .contentType(MediaType.APPLICATION_JSON)
                        .param("startDate", "2024-07-01T10:00:00Z")
                        .param("endTime", "2024-07-01T10:00:00Z"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.status").value(500))
                .andExpect(jsonPath("$.path").value("/api/v1/sensor-readings/average"))
                .andExpect(jsonPath("$.timestamp").exists());

    }

    @Test
    public void testNoMetricsFoundForSpecificSensor() throws Exception {
        // Query empty readings
        mockMvc.perform(get("/api/v1/sensor-readings/average/sensorId")
                        .contentType(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .param("startTime", "2024-07-01T10:00:00Z")
                        .param("endTime", "2024-07-01T10:00:00Z"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.readings").value(0))// Assert that the readings is 0
                .andExpect(jsonPath("$.averageTemperature").doesNotExist()) // Or .value(null) depending on serialization
                .andExpect(jsonPath("$.averageHumidity").doesNotExist())
                .andExpect(jsonPath("$.averageWindSpeed").doesNotExist());
    }

    @Test
    public void testMetricsFoundForSpecificSensorInTimeFrame() throws Exception {
        testSensorReadingCreation();
        Instant start = Instant.now().minus(30,ChronoUnit.MINUTES).truncatedTo(ChronoUnit.SECONDS);;
        Instant end = Instant.now().plus(2,ChronoUnit.HOURS).truncatedTo(ChronoUnit.SECONDS);;

        // Query empty readings
        mockMvc.perform(get("/api/v1/sensor-readings/average/SENSOR_001")
                        .contentType(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .param("startTime", DateTimeFormatter.ISO_INSTANT.format(start))
                        .param("endTime", DateTimeFormatter.ISO_INSTANT.format(end)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.readings").value(2))
                .andExpect(jsonPath("$.averageTemperature").exists())
                .andExpect(jsonPath("$.averageHumidity").exists())
                .andExpect(jsonPath("$.averageWindSpeed").exists());
    }


}