package com.example.weathersensor.repository;

import com.example.weathersensor.dto.AverageMetricsDto;
import com.example.weathersensor.entity.Sensor;
import com.example.weathersensor.entity.SensorReading;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class SensorReadingRepositoryTest {

        @Autowired
        private TestEntityManager entityManager;

        @Autowired
        private SensorReadingRepository repository;

        @Test
        void shouldFindReadingsWithinTimestampRange() {
                // Arrange
                Instant now = Instant.now();
                Instant oneHourAgo = now.minus(1, ChronoUnit.HOURS);
                Instant twoHoursAgo = now.minus(2, ChronoUnit.HOURS);

                Sensor sensor = new Sensor("SENSOR_1", "Berlin", "Europe/Berlin");
                entityManager.persistAndFlush(sensor);

                SensorReading r1 = new SensorReading(sensor, 25.5, 60.0, 10.5, oneHourAgo);
                SensorReading r2 = new SensorReading(sensor, 23.0, 65.0, 12.0, now);
                SensorReading r3 = new SensorReading(sensor, 20.0, 70.0, 8.0, twoHoursAgo);

                entityManager.persistAndFlush(r1);
                entityManager.persistAndFlush(r2);
                entityManager.persistAndFlush(r3);

                // Act
                List<SensorReading> result = repository.findByTimestampBetween(
                                oneHourAgo.minus(30, ChronoUnit.MINUTES),
                                now.plus(1, ChronoUnit.MINUTES));

                // Assert
                assertThat(result).hasSize(2)
                                .extracting(SensorReading::getTemperature)
                                .containsExactlyInAnyOrder(25.5, 23.0);
        }

        @Test
        void shouldCalculateAverageMetricsInGlobalTimeRange() {
                // Arrange
                Instant now = Instant.now();
                Instant oneHourAgo = now.minus(1, ChronoUnit.HOURS);

                Sensor sensor1 = new Sensor("SENSOR_1", "Berlin", "Europe/Berlin");
                Sensor sensor2 = new Sensor("SENSOR_2", "Berlin", "Europe/Berlin");
                entityManager.persistAndFlush(sensor1);
                entityManager.persistAndFlush(sensor2);

                entityManager.persistAndFlush(new SensorReading(sensor1, 20.0, 60.0, 10.0, oneHourAgo));
                entityManager.persistAndFlush(new SensorReading(sensor2, 30.0, 70.0, 20.0, now));

                // Act
                Optional<AverageMetricsDto> result = repository.findAverageMetricsInDateRange(
                                oneHourAgo.minus(1, ChronoUnit.HOURS),
                                now.plus(1, ChronoUnit.HOURS));

                // Assert
                assertThat(result).isPresent();
                AverageMetricsDto metrics = result.get();

                assertThat(metrics.averageTemperature()).isEqualTo(25.0);
                assertThat(metrics.averageHumidity()).isEqualTo(65.0);
                assertThat(metrics.averageWindSpeed()).isEqualTo(15.0);
                assertThat(metrics.readings()).isEqualTo(2L);
        }

        @Test
        void shouldFindAverageTemperatureBySensorTag() {
                // Arrange
                Instant now = Instant.now();
                Instant oneHourAgo = now.minus(1, ChronoUnit.HOURS);

                Sensor sensor = new Sensor("SENSOR_1", "Berlin", "Europe/Berlin");
                entityManager.persistAndFlush(sensor);

                entityManager.persistAndFlush(new SensorReading(sensor, 20.0, 60.0, 10.0, oneHourAgo));
                entityManager.persistAndFlush(new SensorReading(sensor, 30.0, 70.0, 20.0, now));

                // Act
                Optional<Double> avgTemp = repository.findAvgTemperatureBySensorTagBetween(
                                "SENSOR_1",
                                oneHourAgo.minus(1, ChronoUnit.HOURS),
                                now.plus(1, ChronoUnit.HOURS));

                // Assert
                assertThat(avgTemp).isPresent().contains(25.0);
        }

        @Test
        void shouldFindAverageTemperatureByLocation() {
                // Arrange
                Instant now = Instant.now();
                Instant oneHourAgo = now.minus(1, ChronoUnit.HOURS);

                Sensor sensor1 = new Sensor("SENSOR_1", "Berlin", "Europe/Berlin");
                Sensor sensor2 = new Sensor("SENSOR_2", "Berlin", "Europe/Berlin");

                entityManager.persistAndFlush(sensor1);
                entityManager.persistAndFlush(sensor2);

                entityManager.persistAndFlush(new SensorReading(sensor1, 20.0, 60.0, 10.0, oneHourAgo));
                entityManager.persistAndFlush(new SensorReading(sensor2, 30.0, 70.0, 20.0, now));

                // Act
                Optional<Double> avgTemp = repository.findAvgTemperatureByLocationBetween(
                                "Berlin",
                                oneHourAgo.minus(1, ChronoUnit.HOURS),
                                now.plus(1, ChronoUnit.HOURS));

                // Assert
                assertThat(avgTemp).isPresent().contains(25.0);
        }

        @Test
        void shouldFindAverageTemperatureByTimeZone() {
                // Arrange
                Instant now = Instant.now();
                Instant oneHourAgo = now.minus(1, ChronoUnit.HOURS);
                Sensor sensor = new Sensor("SENSOR_1", "Berlin", "Europe/Berlin");
                entityManager.persistAndFlush(sensor);

                entityManager.persistAndFlush(new SensorReading(sensor, 20.0, 60.0, 10.0, oneHourAgo));
                entityManager.persistAndFlush(new SensorReading(sensor, 30.0, 70.0, 20.0, now));

                // Act
                Optional<Double> avg = repository.findAvgTemperatureByTimeZoneBetween(
                                "Europe/Berlin", oneHourAgo.minus(1, ChronoUnit.HOURS), now.plus(1, ChronoUnit.HOURS));

                // Assert
                assertThat(avg).isPresent().contains(25.0);
        }

        @Test
        void shouldReturnEmptyWhenNoReadingsInRange() {
                // Arrange
                Instant start = Instant.now().minus(10, ChronoUnit.DAYS);
                Instant end = Instant.now().minus(9, ChronoUnit.DAYS);

                // Act
                Optional<AverageMetricsDto> result = repository.findAverageMetricsInDateRange(start, end);

                // Assert
                assertThat(result).isPresent(); // Because COUNT will be 0 and AVG will be nulls
                assertThat(result.get().readings()).isEqualTo(0L);
        }

        @Test
        void shouldFindAverageMetricsByTag() {
                // Arrange
                Instant now = Instant.now();
                Sensor sensor = new Sensor("SENSOR_TAG_TEST", "Paris", "Europe/Paris");
                entityManager.persistAndFlush(sensor);

                entityManager.persistAndFlush(new SensorReading(sensor, 10.0, 40.0, 5.0, now));
                entityManager.persistAndFlush(
                                new SensorReading(sensor, 20.0, 60.0, 15.0, now.plus(1, ChronoUnit.MINUTES)));

                // Act
                Optional<AverageMetricsDto> result = repository.findAverageMetricsByTag(
                                "SENSOR_TAG_TEST", now.minus(1, ChronoUnit.HOURS), now.plus(1, ChronoUnit.HOURS));

                // Assert
                assertThat(result).isPresent();
                AverageMetricsDto dto = result.get();
                assertThat(dto.averageTemperature()).isEqualTo(15.0);
                assertThat(dto.averageHumidity()).isEqualTo(50.0);
                assertThat(dto.averageWindSpeed()).isEqualTo(10.0);
                assertThat(dto.readings()).isEqualTo(2L);
        }

        @Test
        void shouldFindAverageMetricsByLocation() {
                // Arrange
                Instant now = Instant.now();
                Sensor sensor = new Sensor("SENSOR_LOC_TEST", "Madrid", "Europe/Madrid");
                entityManager.persistAndFlush(sensor);

                entityManager.persistAndFlush(new SensorReading(sensor, 18.0, 55.0, 7.0, now));
                entityManager.persistAndFlush(
                                new SensorReading(sensor, 22.0, 65.0, 13.0, now.plus(2, ChronoUnit.MINUTES)));

                // Act
                Optional<AverageMetricsDto> result = repository.findAverageMetricsByLocation(
                                "Madrid", now.minus(1, ChronoUnit.HOURS), now.plus(1, ChronoUnit.HOURS));

                // Assert
                assertThat(result).isPresent();
                AverageMetricsDto dto = result.get();
                assertThat(dto.averageTemperature()).isEqualTo(20.0);
                assertThat(dto.averageHumidity()).isEqualTo(60.0);
                assertThat(dto.averageWindSpeed()).isEqualTo(10.0);
                assertThat(dto.readings()).isEqualTo(2L);
        }

        @Test
        void shouldFindAverageMetricsByTimeZone() {
                // Arrange
                Instant now = Instant.now();
                Sensor sensor = new Sensor("SENSOR_TZ_TEST", "Rome", "Europe/Rome");
                entityManager.persistAndFlush(sensor);

                entityManager.persistAndFlush(new SensorReading(sensor, 12.0, 45.0, 6.0, now));
                entityManager.persistAndFlush(
                                new SensorReading(sensor, 18.0, 55.0, 9.0, now.plus(1, ChronoUnit.MINUTES)));

                // Act
                Optional<AverageMetricsDto> result = repository.findAverageMetricsByTimeZone(
                                "Europe/Rome", now.minus(1, ChronoUnit.HOURS), now.plus(1, ChronoUnit.HOURS));

                // Assert
                assertThat(result).isPresent();
                AverageMetricsDto dto = result.get();
                assertThat(dto.averageTemperature()).isEqualTo(15.0);
                assertThat(dto.averageHumidity()).isEqualTo(50.0);
                assertThat(dto.averageWindSpeed()).isEqualTo(7.5);
                assertThat(dto.readings()).isEqualTo(2L);
        }

        @Test
        void shouldFindAverageMetricsFilteredWithAllNulls() {
                // Arrange
                Instant now = Instant.now();
                Sensor sensor = new Sensor("SENSOR_X", "London", "Europe/London");
                entityManager.persistAndFlush(sensor);
                entityManager.persistAndFlush(new SensorReading(sensor, 22.0, 55.0, 12.0, now));

                // Act
                Optional<AverageMetricsDto> result = repository.findAverageMetricsFiltered(
                                now.minus(1, ChronoUnit.HOURS), now.plus(1, ChronoUnit.HOURS),
                                null, null, null);

                // Assert
                assertThat(result).isPresent();
                assertThat(result.get().readings()).isEqualTo(1L);
        }

        @Test
        void shouldFindAverageMetricsFilteredByLocation() {
                // Arrange
                Instant now = Instant.now();
                Sensor sensor = new Sensor("SENSOR_Y", "Lisbon", "Europe/Lisbon");
                entityManager.persistAndFlush(sensor);
                entityManager.persistAndFlush(new SensorReading(sensor, 16.0, 50.0, 8.0, now));

                // Act
                Optional<AverageMetricsDto> result = repository.findAverageMetricsFiltered(
                                now.minus(1, ChronoUnit.HOURS), now.plus(1, ChronoUnit.HOURS),
                                "Lisbon", null, null);

                // Assert
                assertThat(result).isPresent();
                assertThat(result.get().averageTemperature()).isEqualTo(16.0);
        }
}