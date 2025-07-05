package com.example.weathersensor.dto;

import jakarta.validation.constraints.NotBlank;

public record SensorRegistrationRequest(
                @NotBlank String tag,
                @NotBlank String location,
                @NotBlank String timeZone) {
}
