package com.example.weathersensor.service;

import com.example.weathersensor.dto.AverageMetricResponse;
import com.example.weathersensor.dto.AverageMetricsDto;
import com.example.weathersensor.dto.SensorReadingRequest;
import com.example.weathersensor.dto.SensorReadingResponse;
import com.example.weathersensor.entity.Sensor;
import com.example.weathersensor.entity.SensorReading;
import com.example.weathersensor.exception.SensorNotFoundException;
import com.example.weathersensor.repository.SensorReadingRepository;
import com.example.weathersensor.repository.SensorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.*;
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
            throw new SensorNotFoundException("Sensor not found with ID: " + request.sensorId() , SensorNotFoundException.MODE.READING);
        }

        SensorReading reading = new SensorReading(
                sensorOpt.get(),
                request.temperature(),
                request.humidity(),
                request.windSpeed(),
                request.timestamp()
        );

        SensorReading saved = sensorReadingRepository.save(reading);
        saved.getSensor().getTimeZone();

        // Convert to a specific time zone
        ZonedDateTime targetTime = saved.getTimestamp().atZone(ZoneId.of(saved.getSensor().getTimeZone()));
        // Convert back to OffsetDateTime for response
        OffsetDateTime responseTimestamp = saved.getTimestamp().atOffset(ZoneOffset.UTC);

        return new SensorReadingResponse(
                saved.getId(),
                sensorOpt.get().getTag(),
                saved.getTemperature(),
                saved.getHumidity(),
                saved.getWindSpeed(),
                targetTime
        );
    }

    /**
     * Get average metrics for all sensors in a date range
     */
    public AverageMetricsDto getAverageMetrics(Instant startTime, Instant endTime) {


        Optional<AverageMetricsDto> result = sensorReadingRepository.findAverageMetricsInDateRangeV2(startTime, endTime);
        return result.orElseGet(() -> new AverageMetricsDto(null, null, null, 0L));

    }

    /**
     * Get average metrics for a specific sensor in a date range
     */
    public AverageMetricsDto getAverageMetricsBySensor(String sensorId,
                                                           Instant startTime,
                                                           Instant endTime) {


        Optional<AverageMetricsDto> result= sensorReadingRepository.findAverageMetricsFiltered(startTime, endTime,
                null,sensorId,null);



        return result.orElseGet(() -> new AverageMetricsDto(null, null, null, 0L));
    }

    /**
     * Get all sensor readings within a date range
     */
    public List<SensorReadingResponse> getReadings(Instant startTime, Instant endTime) {


        List<SensorReading> readings = sensorReadingRepository.findByTimestampBetween(startTime, endTime);

        return readings.stream()
                .map(this::convertToResponse)
                .toList();
    }

    private SensorReadingResponse convertToResponse(SensorReading reading) {
        OffsetDateTime timestamp = reading.getTimestamp().atOffset(ZoneOffset.UTC);
        ZonedDateTime targetTime = reading.getTimestamp().atZone(ZoneId.of(reading.getSensor().getTimeZone()));
        return new SensorReadingResponse(
                reading.getId(),
                reading.getSensor().getTag(),
                reading.getTemperature(),
                reading.getHumidity(),
                reading.getWindSpeed(),
                targetTime
        );
    }
}