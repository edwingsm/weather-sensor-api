package com.example.weathersensor.service;

import com.example.weathersensor.dto.AverageMetricResponse;
import com.example.weathersensor.dto.SensorReadingRequest;
import com.example.weathersensor.dto.SensorReadingResponse;
import com.example.weathersensor.entity.Sensor;
import com.example.weathersensor.entity.SensorReading;
import com.example.weathersensor.repository.SensorReadingRepository;
import com.example.weathersensor.repository.SensorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;

@Service
public class SensorReadingService {

    private final SensorReadingRepository sensorReadingRepository;
    private final  SensorRepository sensorRepository;

    @Autowired
    public SensorReadingService(SensorReadingRepository sensorReadingRepository, SensorRepository sensorRepository) {
        this.sensorReadingRepository = sensorReadingRepository;
        this.sensorRepository = sensorRepository;
    }

    /**
     * Register a new sensor reading
     * Converts OffsetDateTime to UTC Instant for storage
     */
    public SensorReadingResponse registerReading(SensorReadingRequest request) {
        Optional<Sensor> sensorOpt = sensorRepository.findByTag(request.sensorId());

        if (sensorOpt.isEmpty()) {
            throw new RuntimeException("Sensor not found with ID: " + request.sensorId());
        }
        // Convert OffsetDateTime to UTC Instant for storage
        Instant utcTimestamp = request.timestamp().toInstant();

        SensorReading reading = new SensorReading(
                sensorOpt.get(),
                request.temperature(),
                request.humidity(),
                request.windSpeed(),
                utcTimestamp
        );

        SensorReading saved = sensorReadingRepository.save(reading);

        // Convert back to OffsetDateTime for response
        OffsetDateTime responseTimestamp = saved.getTimestamp().atOffset(ZoneOffset.UTC);

        return new SensorReadingResponse(
                saved.getId(),
                sensorOpt.get().getTag(),
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

        Optional<Object> result = sensorReadingRepository.findAverageMetricsInDateRange(startInstant, endInstant);

//        if (result == null || result[0] == null) {
//            return new AverageMetricResponse(null, null, null, 0L);
//        }
//
//        return new AverageMetricResponse(
//                (Double) result[0],  // average temperature
//                (Double) result[1],  // average humidity
//                (Double) result[2],  // average wind speed
//                (Long) result[3]     // sample count
//        );

        return null;
    }

    /**
     * Get average metrics for a specific sensor in a date range
     */
//    public AverageMetricResponse getAverageMetricsBySensor(String sensorId,
//                                                           OffsetDateTime startTime,
//                                                           OffsetDateTime endTime) {
//        Instant startInstant = startTime.toInstant();
//        Instant endInstant = endTime.toInstant();
//
//        Object[] result = sensorReadingRepository.findAverageMetricsBySensorInDateRange(sensorId, startInstant, endInstant);
//
//        if (result == null || result[0] == null) {
//            return new AverageMetricResponse(null, null, null, 0L);
//        }
//
//        return new AverageMetricResponse(
//                (Double) result[0],  // average temperature
//                (Double) result[1],  // average humidity
//                (Double) result[2],  // average wind speed
//                (Long) result[3]     // sample count
//        );
//    }

    /**
     * Get all sensor readings within a date range
     */
    public List<SensorReadingResponse> getReadings(OffsetDateTime startTime, OffsetDateTime endTime) {
        Instant startInstant = startTime.toInstant();
        Instant endInstant = endTime.toInstant();

        List<SensorReading> readings = sensorReadingRepository.findByTimestampBetween(startInstant, endInstant);

        return readings.stream()
                .map(this::convertToResponse)
                .toList();
    }

    private SensorReadingResponse convertToResponse(SensorReading reading) {
        OffsetDateTime timestamp = reading.getTimestamp().atOffset(ZoneOffset.UTC);

        return new SensorReadingResponse(
                reading.getId(),
                reading.getSensor().getTag(),
                reading.getTemperature(),
                reading.getHumidity(),
                reading.getWindSpeed(),
                timestamp
        );
    }
}