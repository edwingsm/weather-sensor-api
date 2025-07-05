package com.example.weathersensor.service;

import com.example.weathersensor.dto.AverageMetricsDto;
import com.example.weathersensor.dto.SensorReadingRequest;
import com.example.weathersensor.dto.SensorReadingResponse;
import com.example.weathersensor.entity.Sensor;
import com.example.weathersensor.entity.SensorReading;
import com.example.weathersensor.repository.SensorReadingRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.*;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SensorReadingServiceTest {

    @Mock
    private SensorReadingRepository repository;

    @Mock
    private SensorService sensorService;

    @InjectMocks
    private SensorReadingService service;

    @Test
    void shouldRegisterReadingAndConvertTimestampToUtc() {
        // Given
        Instant instant = Instant.now();
        ZonedDateTime targetTime = instant.atZone(ZoneId.of("Europe/Berlin"));
        SensorReadingRequest request = new SensorReadingRequest(
                "SENSOR_1", 25.5, 60.0, 10.5, instant);
        Sensor sensor = new Sensor("SENSOR_1", "Berlin", "Europe/Berlin");
        SensorReading savedReading = new SensorReading(
                sensor, 25.5, 60.0, 10.5, instant);
        savedReading.setId(1L);

        when(repository.save(any(SensorReading.class))).thenReturn(savedReading);
        when(sensorService.getSensorByTag(any(String.class))).thenReturn(Optional.of(sensor));
        // When
        SensorReadingResponse response = service.registerReading(request);

        // Then
        assertThat(response.sensorId()).isEqualTo("SENSOR_1");
        assertThat(response.temperature()).isEqualTo(25.5);
        assertThat(response.humidity()).isEqualTo(60.0);
        assertThat(response.windSpeed()).isEqualTo(10.5);
        assertThat(response.timestamp()).isEqualTo(targetTime);
    }

    @Test
    void shouldGetAverageMetricsForAllSensors() {
        // Given
        OffsetDateTime startTime = OffsetDateTime.parse("2023-12-01T10:00:00Z");
        OffsetDateTime endTime = OffsetDateTime.parse("2023-12-01T18:00:00Z");

        Optional<AverageMetricsDto> mockResult = Optional.of(new AverageMetricsDto(25.5, 65.0, 15.0D, 10L)); // {25.5,
                                                                                                             // 65.0,
                                                                                                             // 15.0,
                                                                                                             // 10L};
        when(repository.findAverageMetricsInDateRange(any(Instant.class), any(Instant.class)))
                .thenReturn(mockResult);

        // When
        AverageMetricsDto response = service.getAverageMetrics(Instant.now(), Instant.now().plus(1, ChronoUnit.DAYS));

        // Then
        assertThat(response.averageTemperature()).isEqualTo(25.5);
        assertThat(response.averageHumidity()).isEqualTo(65.0);
        assertThat(response.averageWindSpeed()).isEqualTo(15.0);
        assertThat(response.readings()).isEqualTo(10L);
    }

    // @Test
    // void shouldReturnEmptyResponseWhenNoDataFound() {
    // // Given
    // OffsetDateTime startTime = OffsetDateTime.parse("2023-12-01T10:00:00Z");
    // OffsetDateTime endTime = OffsetDateTime.parse("2023-12-01T18:00:00Z");
    //
    // when(repository.findAverageMetricsInDateRange(any(Instant.class),
    // any(Instant.class)))
    // .thenReturn(new Object[]{null, null, null, 0L});
    //
    // // When
    // AverageMetricResponse response = service.getAverageMetrics(startTime,
    // endTime);
    //
    // // Then
    // assertThat(response.averageTemperature()).isNull();
    // assertThat(response.averageHumidity()).isNull();
    // assertThat(response.averageWindSpeed()).isNull();
    // assertThat(response.sampleCount()).isEqualTo(0L);
    // }
}