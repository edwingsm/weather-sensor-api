package com.example.weathersensor.dto;

public record AverageMetricsDto(
        Double avgTemperature,
        Double avgHumidity,
        Double avgWindSpeed,
        Long count
) {}