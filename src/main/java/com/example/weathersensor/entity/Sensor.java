package com.example.weathersensor.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.ZoneId;

@Setter
@Getter
@NoArgsConstructor
@Entity
@Table(name = "sensors")
public class Sensor {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String tag;

    @Column(nullable = false)
    private String location;

    @Column(nullable = false)
    private String timeZone;

    public Sensor(String tag, String location, String timeZone) {
        this.tag = tag;
        this.location = location;
        this.timeZone = timeZone;
    }

    public ZoneId getZoneId() {
        return ZoneId.of(this.timeZone);
    }
}