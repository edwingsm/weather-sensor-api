package com.example.weathersensor.integration;

import com.example.weathersensor.WeatherSensorApiApplication;
import com.example.weathersensor.dto.SensorRegistrationRequest;
import com.example.weathersensor.dto.SensorResponseDto;
import com.example.weathersensor.entity.Sensor;
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
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = WeatherSensorApiApplication.class)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
@ActiveProfiles("test")
public class SensorControllerIntegrationTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private SensorRepository sensorRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private MockMvc mockMvc;

    @BeforeEach
    public void setup() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        sensorRepository.deleteAll();
        // Register Sensor
        Sensor sensor = new Sensor("SENSOR_001", "Berlin", "Europe/Berlin");
        Sensor sensor2 = new Sensor("SENSOR_002", "Berlin", "Europe/Berlin");
        Sensor sensor3 = new Sensor("SENSOR_003", "Delhi", "Asia/Kolkata");
        sensorRepository.saveAll(Arrays.asList(sensor, sensor2, sensor3));
    }

    @Test
    public void testSensorCreation() throws Exception {
        SensorRegistrationRequest request = new SensorRegistrationRequest("SENSOR_004", "London", "Europe/London");
        mockMvc.perform(post("/api/v1/sensors")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.tag").value("SENSOR_004"))
                .andExpect(jsonPath("$.location").value("London"))
                .andExpect(jsonPath("$.timeZone").value("Europe/London"));
        assertEquals(4, sensorRepository.count());
    }

    @Test
    public void testFindSensorByLocation() throws Exception {
        assertEquals(3, sensorRepository.count());
        mockMvc.perform(get("/api/v1/sensors/location/Berlin"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").exists())
                .andExpect(jsonPath("$[0].tag").value("SENSOR_001"))
                .andExpect(jsonPath("$[0].location").value("Berlin"))
                .andExpect(jsonPath("$[0].timeZone").value("Europe/Berlin"))
                .andExpect(jsonPath("$[1].id").exists())
                .andExpect(jsonPath("$[1].tag").value("SENSOR_002"))
                .andExpect(jsonPath("$[1].location").value("Berlin"))
                .andExpect(jsonPath("$[1].timeZone").value("Europe/Berlin"));
        ;
    }

    @Test
    public void testListAllSensors() throws Exception {
        assertEquals(3, sensorRepository.count());
        mockMvc.perform(get("/api/v1/sensors"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(3))
                .andExpect(jsonPath("$[*].tag")
                        .value(org.hamcrest.Matchers.hasItems("SENSOR_001", "SENSOR_002", "SENSOR_003")))
                .andExpect(jsonPath("$[*].location").value(org.hamcrest.Matchers.hasItems("Berlin", "Berlin", "Delhi")))
                .andExpect(jsonPath("$[*].timeZone")
                        .value(org.hamcrest.Matchers.hasItems("Europe/Berlin", "Asia/Kolkata")));
        ;
    }

    @Test
    public void testRegisterSensor_Conflict_WhenSensorAlreadyExists() throws Exception {
        SensorRegistrationRequest request = new SensorRegistrationRequest("SENSOR_001", "Berlin", "Europe/Berlin");

        mockMvc.perform(post("/api/v1/sensors")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Sensor with tag 'SENSOR_001' already exists"));
    }

    @Test
    public void testRegisterSensor_BadRequest_WhenFieldsMissing() throws Exception {
        // Invalid: tag is null
        String invalidPayload = """
            {
              "location": "Madrid",
              "timeZone": "Europe/Madrid"
            }
            """;

        mockMvc.perform(post("/api/v1/sensors")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidPayload))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    public void testRegisterSensor_BadRequest_WhenFieldsMissing2() throws Exception {
        // Invalid: tag is null
        String invalidPayload = """
                { "tag": "SENSOR_001", "location": "Berlin" "timeZone": "Europe/Berlin" }
            """;

        mockMvc.perform(post("/api/v1/sensors")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidPayload))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    public void testGetSensorsByLocation_NotFound() throws Exception {
        mockMvc.perform(get("/api/v1/sensors/location/Tokyo"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    public void testListAllSensors_Empty() throws Exception {
        sensorRepository.deleteAll();

        mockMvc.perform(get("/api/v1/sensors"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }
}
