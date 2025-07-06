package com.example.weathersensor.dto;

public record SensorResponseDto(
                Long id,
                String tag,
                String location,
                String timeZone) {
}
