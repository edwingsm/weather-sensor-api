package com.example.weathersensor.service;

import com.example.weathersensor.dto.AverageMetricsDto;
import com.example.weathersensor.dto.SensorReadingRequest;
import com.example.weathersensor.dto.SensorReadingResponse;
import com.example.weathersensor.entity.Sensor;
import com.example.weathersensor.entity.SensorReading;
import com.example.weathersensor.exception.SensorNotFoundException;
import com.example.weathersensor.repository.SensorReadingRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.*;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

@Service
public class SensorReadingService {

    private final SensorReadingRepository sensorReadingRepository;
    private final SensorService sensorService;

    @Autowired
    public SensorReadingService(SensorReadingRepository sensorReadingRepository, SensorService sensorService) {
        this.sensorReadingRepository = sensorReadingRepository;
        this.sensorService = sensorService;
    }

    /**
     * Register a new sensor reading
     * Converts OffsetDateTime to UTC Instant for storage
     */
    public SensorReadingResponse registerReading(SensorReadingRequest request) {
        Optional<Sensor> sensorOpt = sensorService.getSensorByTag(request.sensorId());

        if (sensorOpt.isEmpty()) {
            throw new SensorNotFoundException("Sensor not found with ID: " + request.sensorId(),
                    SensorNotFoundException.MODE.READING);
        }

        SensorReading reading = new SensorReading(
                sensorOpt.get(),
                request.temperature(),
                request.humidity(),
                request.windSpeed(),
                request.timestamp().truncatedTo(ChronoUnit.SECONDS));

        SensorReading saved = sensorReadingRepository.save(reading);
        saved.getSensor().getTimeZone();
        // Convert to a specific time zone
        ZonedDateTime targetTime = saved.getTimestamp().atZone(ZoneId.of(saved.getSensor().getTimeZone()));

        return new SensorReadingResponse(
                saved.getId(),
                sensorOpt.get().getTag(),
                saved.getTemperature(),
                saved.getHumidity(),
                saved.getWindSpeed(),
                targetTime);
    }

    /**
     * Get average metrics for all sensors in a date range
     */
    public AverageMetricsDto getAverageMetrics(Instant startTime, Instant endTime) {

        Optional<AverageMetricsDto> result = sensorReadingRepository.findAverageMetricsInDateRange(startTime,
                endTime);
        return result.orElseGet(() -> new AverageMetricsDto(null, null, null, 0L));

    }

    /**
     * Get average metrics for a specific sensor in a date range
     */
    public AverageMetricsDto getAverageMetricsBySensor(String sensorId,
            Instant startTime,
            Instant endTime) {

        Optional<AverageMetricsDto> result = sensorReadingRepository.findAverageMetricsFiltered(startTime, endTime,
                null, sensorId, null);

        return result.orElseGet(() -> new AverageMetricsDto(null, null, null, 0L));
    }

    public List<SensorReadingResponse> getReadings(Instant startTime, Instant endTime) {
        List<SensorReading> readings = sensorReadingRepository.findByTimestampBetween(startTime, endTime);
        return readings.stream()
                .map(this::convertToResponse)
                .toList();
    }

    private SensorReadingResponse convertToResponse(SensorReading reading) {
        ZonedDateTime targetTime = reading.getTimestamp().truncatedTo(ChronoUnit.SECONDS)
                .atZone(ZoneId.of(reading.getSensor().getTimeZone()));
        return new SensorReadingResponse(
                reading.getId(),
                reading.getSensor().getTag(),
                reading.getTemperature(),
                reading.getHumidity(),
                reading.getWindSpeed(),
                targetTime);
    }
}