package com.example.weathersensor.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotBlank;
import java.time.OffsetDateTime;

public record SensorReadingRequest(
        @NotBlank(message = "Sensor ID is required")
        String sensorId,

        @NotNull(message = "Temperature is required")
        Double temperature,

        @NotNull(message = "Humidity is required")
        Double humidity,

        @NotNull(message = "Wind speed is required")
        Double windSpeed,

        @NotNull(message = "Timestamp is required")
        OffsetDateTime timestamp
) {}