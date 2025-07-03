package com.example.weathersensor.repository;

import com.example.weathersensor.entity.SensorReading;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

@Repository
public interface SensorReadingRepository extends JpaRepository<SensorReading, Long> {

    /**
     * Find all sensor readings within a date range
     */
    List<SensorReading> findByTimestampBetween(Instant startTime, Instant endTime);

    /**
     * Find sensor readings for a specific sensor within a date range
     */
    List<SensorReading> findBySensorIdAndTimestampBetween(String sensorId, Instant startTime, Instant endTime);

    /**
     * Calculate average metrics for all sensors in a date range
     */
    @Query("SELECT AVG(sr.temperature), AVG(sr.humidity), AVG(sr.windSpeed), COUNT(sr) " +
            "FROM SensorReading sr WHERE sr.timestamp BETWEEN :startTime AND :endTime")
    Object[] findAverageMetricsInDateRange(@Param("startTime") Instant startTime,
                                           @Param("endTime") Instant endTime);

    /**
     * Calculate average metrics for a specific sensor in a date range
     */
    @Query("SELECT AVG(sr.temperature), AVG(sr.humidity), AVG(sr.windSpeed), COUNT(sr) " +
            "FROM SensorReading sr WHERE sr.sensorId = :sensorId AND sr.timestamp BETWEEN :startTime AND :endTime")
    Object[] findAverageMetricsBySensorInDateRange(@Param("sensorId") String sensorId,
                                                   @Param("startTime") Instant startTime,
                                                   @Param("endTime") Instant endTime);
}
