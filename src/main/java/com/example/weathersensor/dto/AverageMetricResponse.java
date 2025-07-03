package com.example.weathersensor.dto;

public record AverageMetricResponse(
        Double averageTemperature,
        Double averageHumidity,
        Double averageWindSpeed,
        Long sampleCount
) {}

