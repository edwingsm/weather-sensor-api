package com.example.weathersensor.dto;

import jakarta.validation.constraints.NotBlank;

public record SensorRegistrationDto(
        @NotBlank String tag,
        @NotBlank String location,
        @NotBlank String timeZone
) {}
