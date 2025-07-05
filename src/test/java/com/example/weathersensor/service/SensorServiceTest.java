package com.example.weathersensor.service;

import com.example.weathersensor.dto.SensorReadingRequest;
import com.example.weathersensor.dto.SensorReadingResponse;
import com.example.weathersensor.dto.SensorRegistrationDto;
import com.example.weathersensor.dto.SensorResponseDto;
import com.example.weathersensor.entity.Sensor;
import com.example.weathersensor.entity.SensorReading;
import com.example.weathersensor.repository.SensorRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class SensorServiceTest {

    @Mock
    private SensorRepository sensorRepository;

    @InjectMocks
    private SensorService service;


    @Test
    void shouldRegisterReadingAndConvertTimestampToUtc() {
        // Given
        OffsetDateTime clientTime = OffsetDateTime.parse("2023-12-01T15:30:00+02:00");
        Sensor sensor = new Sensor("SENSOR_1","Berlin","Europe/Berlin");
        sensor.setId(1l);
        when(sensorRepository.save(any(Sensor.class))).thenReturn(sensor);
        //when(sensorRepository.findByTag(any(String.class))).thenReturn(Optional.of(sensor));

        SensorRegistrationDto sensorRegistrationDto = new SensorRegistrationDto("SENSOR_1","Berlin","Europe/Berlin");
        // When
        SensorResponseDto response = service.registerSensor(sensorRegistrationDto);

        // Then
        assertThat(response.id()).isEqualTo(1l);
        assertThat(response.tag()).isEqualTo("SENSOR_1");
        assertThat(response.location()).isEqualTo("Berlin");
        assertThat(response.timeZone()).isEqualTo("Europe/Berlin");
    }

    @Test
    void shouldRegisterReadingAndConvertTimestampToUtc2() {
        // Given
        OffsetDateTime clientTime = OffsetDateTime.parse("2023-12-01T15:30:00+02:00");
        Sensor sensor = new Sensor("SENSOR_1","Berlin","Europe/Berlin");
        sensor.setId(1l);
        when(sensorRepository.save(any(Sensor.class))).thenReturn(sensor);
        //when(sensorRepository.findByTag(any(String.class))).thenReturn(Optional.of(sensor));

        SensorRegistrationDto sensorRegistrationDto = new SensorRegistrationDto("SENSOR_1","Berlin","Europe/Berlin");
        // When
        SensorResponseDto response = service.registerSensor(sensorRegistrationDto);

        // Then
        assertThat(response.id()).isEqualTo(1l);
        assertThat(response.tag()).isEqualTo("SENSOR_1");
        assertThat(response.location()).isEqualTo("Berlin");
        assertThat(response.timeZone()).isEqualTo("Europe/Berlin");
    }
}
