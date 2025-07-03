package com.example.weathersensor.service;

import com.example.weathersensor.dto.AverageMetricResponse;
import com.example.weathersensor.dto.SensorReadingRequest;
import com.example.weathersensor.dto.SensorReadingResponse;
import com.example.weathersensor.entity.SensorReading;
import com.example.weathersensor.repository.SensorReadingRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SensorReadingServiceTest {

    @Mock
    private SensorReadingRepository repository;

    @InjectMocks
    private SensorReadingService service;

    @Test
    void shouldRegisterReadingAndConvertTimestampToUtc() {
        // Given
        OffsetDateTime clientTime = OffsetDateTime.parse("2023-12-01T15:30:00+02:00");
        SensorReadingRequest request = new SensorReadingRequest(
                "SENSOR_1", 25.5, 60.0, 10.5, clientTime
        );

        SensorReading savedReading = new SensorReading(
                "SENSOR_1", 25.5, 60.0, 10.5, clientTime.toInstant()
        );
        savedReading.setId(1L);

        when(repository.save(any(SensorReading.class))).thenReturn(savedReading);

        // When
        SensorReadingResponse response = service.registerReading(request);

        // Then
        assertThat(response.sensorId()).isEqualTo("SENSOR_1");
        assertThat(response.temperature()).isEqualTo(25.5);
        assertThat(response.humidity()).isEqualTo(60.0);
        assertThat(response.windSpeed()).isEqualTo(10.5);
        assertThat(response.timestamp()).isEqualTo(clientTime.toInstant().atOffset(ZoneOffset.UTC));
    }

    @Test
    void shouldGetAverageMetricsForAllSensors() {
        // Given
        OffsetDateTime startTime = OffsetDateTime.parse("2023-12-01T10:00:00Z");
        OffsetDateTime endTime = OffsetDateTime.parse("2023-12-01T18:00:00Z");

        Object[] mockResult = {25.5, 65.0, 15.0, 10L};
        when(repository.findAverageMetricsInDateRange(any(Instant.class), any(Instant.class)))
                .thenReturn(mockResult);

        // When
        AverageMetricResponse response = service.getAverageMetrics(startTime, endTime);

        // Then
        assertThat(response.averageTemperature()).isEqualTo(25.5);
        assertThat(response.averageHumidity()).isEqualTo(65.0);
        assertThat(response.averageWindSpeed()).isEqualTo(15.0);
        assertThat(response.sampleCount()).isEqualTo(10L);
    }

    @Test
    void shouldReturnEmptyResponseWhenNoDataFound() {
        // Given
        OffsetDateTime startTime = OffsetDateTime.parse("2023-12-01T10:00:00Z");
        OffsetDateTime endTime = OffsetDateTime.parse("2023-12-01T18:00:00Z");

        when(repository.findAverageMetricsInDateRange(any(Instant.class), any(Instant.class)))
                .thenReturn(new Object[]{null, null, null, 0L});

        // When
        AverageMetricResponse response = service.getAverageMetrics(startTime, endTime);

        // Then
        assertThat(response.averageTemperature()).isNull();
        assertThat(response.averageHumidity()).isNull();
        assertThat(response.averageWindSpeed()).isNull();
        assertThat(response.sampleCount()).isEqualTo(0L);
    }
}