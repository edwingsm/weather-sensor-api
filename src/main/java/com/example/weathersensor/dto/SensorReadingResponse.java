package com.example.weathersensor.dto;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZonedDateTime;

public record SensorReadingResponse(
                Long id,
                String sensorId,
                Double temperature,
                Double humidity,
                Double windSpeed,
                ZonedDateTime timestamp) {
}