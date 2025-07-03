package com.example.weathersensor.dto;

import java.time.OffsetDateTime;

public record SensorReadingResponse(
        Long id,
        String sensorId,
        Double temperature,
        Double humidity,
        Double windSpeed,
        OffsetDateTime timestamp
) {}