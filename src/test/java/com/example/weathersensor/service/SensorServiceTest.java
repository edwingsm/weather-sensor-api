package com.example.weathersensor.service;

import com.example.weathersensor.dto.SensorRegistrationRequest;
import com.example.weathersensor.dto.SensorResponseDto;
import com.example.weathersensor.entity.Sensor;
import com.example.weathersensor.exception.SensorAlreadyExistsException;
import com.example.weathersensor.repository.SensorRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
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
        SensorRegistrationRequest sensorRegistrationRequest = new SensorRegistrationRequest("SENSOR_1","Berlin","Europe/Berlin");
        // When
        SensorResponseDto response = service.registerSensor(sensorRegistrationRequest);

        // Then
        assertThat(response.id()).isEqualTo(1l);
        assertThat(response.tag()).isEqualTo("SENSOR_1");
        assertThat(response.location()).isEqualTo("Berlin");
        assertThat(response.timeZone()).isEqualTo("Europe/Berlin");
    }

    @Test
    void shouldRegisterReadingAndConvertTimestampToUtc2() {
        SensorRegistrationRequest mockRequest = mock(SensorRegistrationRequest.class);
        when(mockRequest.tag()).thenReturn("SENSOR_001");
        when(sensorRepository.existsByTag("SENSOR_001")).thenReturn(true);

        assertThatThrownBy(() -> service.registerSensor(mockRequest))
                .isInstanceOf(SensorAlreadyExistsException.class)
                .hasMessageContaining("SENSOR_001");

    }

    @Test
    void testGetAllSensors(){
        Sensor sensor = new Sensor("SENSOR_001", "Berlin", "Europe/Berlin");
        sensor.setId(1l);
        when(sensorRepository.findAll()).thenReturn(Arrays.asList(sensor));


        // When
        List<SensorResponseDto> response = service.getAllSensors();

        // Then
        assertThat(response).isNotEmpty();
        assertThat(response.get(0).id()).isEqualTo(1l);
        assertThat(response.get(0).tag()).isEqualTo("SENSOR_001");
        assertThat(response.get(0).location()).isEqualTo("Berlin");
        assertThat(response.get(0).timeZone()).isEqualTo("Europe/Berlin");
    }

    @Test
    void testGetSensorByTag(){
        Sensor sensor = new Sensor("SENSOR_001", "Berlin", "Europe/Berlin");
        sensor.setId(1l);
        when(sensorRepository.findByTag(any(String.class))).thenReturn(Optional.of(sensor));

        // When
        Optional<Sensor> response = service.getSensorByTag("SENSOR_001");

        // Then
        assertThat(response.get().getId()).isEqualTo(1l);
        assertThat(response.get().getTag()).isEqualTo("SENSOR_001");
        assertThat(response.get().getLocation()).isEqualTo("Berlin");
        assertThat(response.get().getTimeZone()).isEqualTo("Europe/Berlin");
    }

    @Test
    void testGetSensorsByLocation(){
        Sensor sensor = new Sensor("SENSOR_001", "Berlin", "Europe/Berlin");
        sensor.setId(1l);
        when(sensorRepository.findByLocation(any(String.class))).thenReturn(Arrays.asList(sensor));


        // When
        List<SensorResponseDto> response = service.getSensorsByLocation("Berlin");

        // Then
        assertThat(response).isNotEmpty();
        assertThat(response.get(0).id()).isEqualTo(1l);
        assertThat(response.get(0).tag()).isEqualTo("SENSOR_001");
        assertThat(response.get(0).location()).isEqualTo("Berlin");
        assertThat(response.get(0).timeZone()).isEqualTo("Europe/Berlin");
    }


}
