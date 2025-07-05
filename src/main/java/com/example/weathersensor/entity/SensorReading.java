package com.example.weathersensor.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@NoArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "sensor_readings", uniqueConstraints = {
        @UniqueConstraint(columnNames = { "sensor_tag", "timestamp" })
})
public class SensorReading {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sensor_tag", referencedColumnName = "tag", nullable = false)
    private Sensor sensor;

    @Column(nullable = false)
    private Double temperature;

    @Column(nullable = false)
    private Double humidity;

    @Column(nullable = false)
    private Double windSpeed;

    @Column(nullable = false)
    private Instant timestamp = Instant.now();

    public SensorReading(Sensor sensor, Double temperature, Double humidity, Double windSpeed, Instant timestamp) {
        this.sensor = sensor;
        this.temperature = temperature;
        this.humidity = humidity;
        this.windSpeed = windSpeed;
        this.timestamp = timestamp;
    }
}