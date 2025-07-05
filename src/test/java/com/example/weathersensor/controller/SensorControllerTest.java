package com.example.weathersensor.controller;

import com.example.weathersensor.config.TestSecurityConfig;
import com.example.weathersensor.dto.SensorRegistrationRequest;
import com.example.weathersensor.dto.SensorResponseDto;
import com.example.weathersensor.entity.Sensor;
import com.example.weathersensor.service.SensorService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.lang.reflect.Array;
import java.util.Arrays;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

//import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;

@AutoConfigureMockMvc(addFilters = false)
@WebMvcTest(SensorController.class)
//@ActiveProfiles("test")
//@Import(TestSecurityConfig.class)
public class SensorControllerTest {
        @Autowired
        private MockMvc mockMvc;

        @MockitoBean
        private SensorService sensorService;

        @Autowired
        private ObjectMapper objectMapper;

        @Test
        public void testRegisterSensor() throws Exception {
                SensorResponseDto SensorResponseDto = new SensorResponseDto(1l, "SENSOR_001", "Berlin",
                                "Europe/Berlin");
                when(sensorService.registerSensor(any(SensorRegistrationRequest.class)))
                                .thenReturn(SensorResponseDto);
                SensorRegistrationRequest request = new SensorRegistrationRequest("SENSOR_001", "Berlin",
                                "Europe/Berlin");

                mockMvc.perform(post("/api/v1/sensors")
  //                              .with(httpBasic("admin", "password"))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isCreated())
                                .andExpect(jsonPath("$.id").value(Long.valueOf(1l)))
                                .andExpect(jsonPath("$.tag").value("SENSOR_001"))
                                .andExpect(jsonPath("$.location").value("Berlin"))
                                .andExpect(jsonPath("$.timeZone").value("Europe/Berlin"));
        }

        @Test
        public void testFindSensorByLocation() throws Exception {
                SensorResponseDto sensorResponseDto = new SensorResponseDto(1l, "SENSOR_001", "Berlin",
                                "Europe/Berlin");
                when(sensorService.getSensorsByLocation(any(String.class)))
                                .thenReturn(Arrays.asList(sensorResponseDto));

                mockMvc.perform(get("/api/v1/sensors/location/Berlin"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.length()").value(1))
                                .andExpect(jsonPath("$[0].id").value(1L))
                                .andExpect(jsonPath("$[0].tag").value("SENSOR_001"))
                                .andExpect(jsonPath("$[0].location").value("Berlin"))
                                .andExpect(jsonPath("$[0].timeZone").value("Europe/Berlin"));
        }

        @Test
        public void testListAllSensors() throws Exception {
                SensorResponseDto sensorResponseDto = new SensorResponseDto(1l, "SENSOR_001", "Berlin",
                                "Europe/Berlin");
                when(sensorService.getAllSensors())
                                .thenReturn(Arrays.asList(sensorResponseDto));

                mockMvc.perform(get("/api/v1/sensors"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.length()").value(1))
                                .andExpect(jsonPath("$[0].id").value(1L))
                                .andExpect(jsonPath("$[0].tag").value("SENSOR_001"))
                                .andExpect(jsonPath("$[0].location").value("Berlin"))
                                .andExpect(jsonPath("$[0].timeZone").value("Europe/Berlin"));
        }

}
