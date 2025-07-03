package com.example.weathersensor.service;

import com.example.weathersensor.dto.AverageMetricResponse;
import com.example.weathersensor.dto.SensorReadingRequest;
import com.example.weathersensor.dto.SensorReadingResponse;
import com.example.weathersensor.entity.SensorReading;
import com.example.weathersensor.repository.SensorReadingRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;

@Service
public class SensorReadingService {

    private final SensorReadingRepository repository;

    @Autowired
    public SensorReadingService(SensorReadingRepository repository) {
        this.repository = repository;
    }

    /**
     * Register a new sensor reading
     * Converts OffsetDateTime to UTC Instant for storage
     */
    public SensorReadingResponse registerReading(SensorReadingRequest request) {
        // Convert OffsetDateTime to UTC Instant for storage
        Instant utcTimestamp = request.timestamp().toInstant();

        SensorReading reading = new SensorReading(
                request.sensorId(),
                request.temperature(),
                request.humidity(),
                request.windSpeed(),
                utcTimestamp
        );

        SensorReading saved = repository.save(reading);

        // Convert back to OffsetDateTime for response
        OffsetDateTime responseTimestamp = saved.getTimestamp().atOffset(ZoneOffset.UTC);

        return new SensorReadingResponse(
                saved.getId(),
                saved.getSensorId(),
                saved.getTemperature(),
                saved.getHumidity(),
                saved.getWindSpeed(),
                responseTimestamp
        );
    }

    /**
     * Get average metrics for all sensors in a date range
     */
    public AverageMetricResponse getAverageMetrics(OffsetDateTime startTime, OffsetDateTime endTime) {
        Instant startInstant = startTime.toInstant();
        Instant endInstant = endTime.toInstant();

        Object[] result = repository.findAverageMetricsInDateRange(startInstant, endInstant);

        if (result == null || result[0] == null) {
            return new AverageMetricResponse(null, null, null, 0L);
        }

        return new AverageMetricResponse(
                (Double) result[0],  // average temperature
                (Double) result[1],  // average humidity
                (Double) result[2],  // average wind speed
                (Long) result[3]     // sample count
        );
    }

    /**
     * Get average metrics for a specific sensor in a date range
     */
    public AverageMetricResponse getAverageMetricsBySensor(String sensorId,
                                                           OffsetDateTime startTime,
                                                           OffsetDateTime endTime) {
        Instant startInstant = startTime.toInstant();
        Instant endInstant = endTime.toInstant();

        Object[] result = repository.findAverageMetricsBySensorInDateRange(sensorId, startInstant, endInstant);

        if (result == null || result[0] == null) {
            return new AverageMetricResponse(null, null, null, 0L);
        }

        return new AverageMetricResponse(
                (Double) result[0],  // average temperature
                (Double) result[1],  // average humidity
                (Double) result[2],  // average wind speed
                (Long) result[3]     // sample count
        );
    }

    /**
     * Get all sensor readings within a date range
     */
    public List<SensorReadingResponse> getReadings(OffsetDateTime startTime, OffsetDateTime endTime) {
        Instant startInstant = startTime.toInstant();
        Instant endInstant = endTime.toInstant();

        List<SensorReading> readings = repository.findByTimestampBetween(startInstant, endInstant);

        return readings.stream()
                .map(this::convertToResponse)
                .toList();
    }

    private SensorReadingResponse convertToResponse(SensorReading reading) {
        OffsetDateTime timestamp = reading.getTimestamp().atOffset(ZoneOffset.UTC);

        return new SensorReadingResponse(
                reading.getId(),
                reading.getSensorId(),
                reading.getTemperature(),
                reading.getHumidity(),
                reading.getWindSpeed(),
                timestamp
        );
    }
}