package com.example.weathersensor.repository;

import com.example.weathersensor.dto.AverageMetricsDto;
import com.example.weathersensor.entity.SensorReading;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
public interface SensorReadingRepository extends JpaRepository<SensorReading, Long> {

    /**
     * Find all sensor readings within a date range
     */
    List<SensorReading> findByTimestampBetween(Instant startTime, Instant endTime);

    /**
     * Calculate average metrics for all sensors in a date range
     */
    @Query("SELECT AVG(sr.temperature), AVG(sr.humidity), AVG(sr.windSpeed), COUNT(sr) " +
            "FROM SensorReading sr WHERE sr.timestamp BETWEEN :startTime AND :endTime")
    Optional<Object> findAverageMetricsInDateRange(@Param("startTime") Instant startTime,
                                           @Param("endTime") Instant endTime);


    @Query("SELECT AVG(r.temperature) FROM SensorReading r " +
            "WHERE r.sensor.tag = :tag AND r.timestamp BETWEEN :start AND :end")
    Optional<Double> findAvgTemperatureBySensorTagBetween(
            @Param("tag") String tag,
            @Param("start") Instant start,
            @Param("end") Instant end);


    @Query("SELECT AVG(r.temperature) FROM SensorReading r " +
            "WHERE r.sensor.location = :location AND r.timestamp BETWEEN :start AND :end")
    Optional<Double> findAvgTemperatureByLocationBetween(@Param("location") String location,
                                               @Param("start") Instant start,
                                               @Param("end") Instant end);

    @Query("SELECT AVG(r.temperature) FROM SensorReading r " +
            "WHERE r.sensor.timeZone = :timeZone AND r.timestamp BETWEEN :start AND :end")
    Optional<Double> findAvgTemperatureByTimeZoneBetween(@Param("timeZone") String timeZone,
                                               @Param("start") Instant start,
                                               @Param("end") Instant end);



    /**
     * Calculate average metrics for a specific sensor in a date range
     */

    @Query("SELECT new com.example.weathersensor.dto.AverageMetricsDto(AVG(sr.temperature), AVG(sr.humidity), AVG(sr.windSpeed), COUNT(sr)) " +
            "FROM SensorReading sr WHERE sr.timestamp BETWEEN :startTime AND :endTime")
    Optional<AverageMetricsDto> findAverageMetricsInDateRange2(
            @Param("startTime") Instant startTime,
            @Param("endTime") Instant endTime);

    @Query("""
    SELECT new com.example.weathersensor.dto.AverageMetricsDto(
        AVG(sr.temperature), AVG(sr.humidity), AVG(sr.windSpeed), COUNT(sr))
    FROM SensorReading sr
    WHERE sr.timestamp BETWEEN :startTime AND :endTime
      AND (:location IS NULL OR sr.sensor.location = :location)
      AND (:tag IS NULL OR sr.sensor.tag = :tag)
      AND (:timeZone IS NULL OR sr.sensor.timeZone = :timeZone)
    """)
    Optional<AverageMetricsDto> findAverageMetricsFiltered(
            @Param("startTime") Instant startTime,
            @Param("endTime") Instant endTime,
            @Param("location") String location,
            @Param("tag") String tag,
            @Param("timeZone") String timeZone);


    @Query("""
    SELECT new com.example.weathersensor.dto.AverageMetricsDto(
        AVG(sr.temperature), AVG(sr.humidity), AVG(sr.windSpeed), COUNT(sr))
    FROM SensorReading sr
    WHERE sr.sensor.location = :location
      AND sr.timestamp BETWEEN :startTime AND :endTime
    """)
    Optional<AverageMetricsDto> findAverageMetricsByLocation(
            @Param("location") String location,
            @Param("startTime") Instant startTime,
            @Param("endTime") Instant endTime);


    @Query("""
    SELECT new com.example.weathersensor.dto.AverageMetricsDto(
        AVG(sr.temperature), AVG(sr.humidity), AVG(sr.windSpeed), COUNT(sr))
    FROM SensorReading sr
    WHERE sr.sensor.tag = :tag
      AND sr.timestamp BETWEEN :startTime AND :endTime
    """)
    Optional<AverageMetricsDto> findAverageMetricsByTag(
            @Param("tag") String tag,
            @Param("startTime") Instant startTime,
            @Param("endTime") Instant endTime);

    @Query("""
    SELECT new com.example.weathersensor.dto.AverageMetricsDto(
        AVG(sr.temperature), AVG(sr.humidity), AVG(sr.windSpeed), COUNT(sr))
    FROM SensorReading sr
    WHERE sr.sensor.timeZone = :timeZone
      AND sr.timestamp BETWEEN :startTime AND :endTime
    """)
    Optional<AverageMetricsDto> findAverageMetricsByTimeZone(
            @Param("timeZone") String timeZone,
            @Param("startTime") Instant startTime,
            @Param("endTime") Instant endTime);
}
