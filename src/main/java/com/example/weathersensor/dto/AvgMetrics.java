package com.example.weathersensor.dto;

public record AvgMetrics(
        double averageTemperature,
        double averageHumidity,
        double averageWindSpeed,
        int datapoint
) {
}
