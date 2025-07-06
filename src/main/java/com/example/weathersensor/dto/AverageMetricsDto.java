package com.example.weathersensor.dto;

public record AverageMetricsDto(
                Double averageTemperature,
                Double averageHumidity,
                Double averageWindSpeed,
                Long readings) {
}