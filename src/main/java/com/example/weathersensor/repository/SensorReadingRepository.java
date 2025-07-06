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
   * List all reading in specific time interval
   * 
   * @param startTime
   * @param endTime
   * @return
   */
  List<SensorReading> findByTimestampBetween(Instant startTime, Instant endTime);

  /**
   * Query Average Temperature in specific sensor tag
   * 
   * @param tag
   * @param start
   * @param end
   * @return
   */
  @Query("SELECT AVG(r.temperature) FROM SensorReading r " +
      "WHERE r.sensor.tag = :tag AND r.timestamp BETWEEN :start AND :end")
  Optional<Double> findAvgTemperatureBySensorTagBetween(
      @Param("tag") String tag,
      @Param("start") Instant start,
      @Param("end") Instant end);

  /**
   * Query Average Temperature in specific location
   * 
   * @param location
   * @param start
   * @param end
   * @return
   */
  @Query("SELECT AVG(r.temperature) FROM SensorReading r " +
      "WHERE r.sensor.location = :location AND r.timestamp BETWEEN :start AND :end")
  Optional<Double> findAvgTemperatureByLocationBetween(@Param("location") String location,
      @Param("start") Instant start,
      @Param("end") Instant end);

  /**
   * Query Average Temperature in specific timezone
   * 
   * @param timeZone
   * @param start
   * @param end
   * @return
   */

  @Query("SELECT AVG(r.temperature) FROM SensorReading r " +
      "WHERE r.sensor.timeZone = :timeZone AND r.timestamp BETWEEN :start AND :end")
  Optional<Double> findAvgTemperatureByTimeZoneBetween(@Param("timeZone") String timeZone,
      @Param("start") Instant start,
      @Param("end") Instant end);

  /**
   * Query All metics in specific time interval
   * 
   * @param startTime
   * @param endTime
   * @return
   */
  @Query("""
      SELECT new com.example.weathersensor.dto.AverageMetricsDto(AVG(sr.temperature), AVG(sr.humidity), AVG(sr.windSpeed), COUNT(sr))
      FROM SensorReading sr WHERE sr.timestamp BETWEEN :startTime AND :endTime
      """)
  Optional<AverageMetricsDto> findAverageMetricsInDateRange(
      @Param("startTime") Instant startTime,
      @Param("endTime") Instant endTime);

  /**
   * This is a special query used
   * 
   * @param startTime
   * @param endTime
   * @param location
   * @param tag
   * @param timeZone
   * @return
   */
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

  // All 3 remaining queries are individual break down of
  // findAverageMetricsFiltered

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
