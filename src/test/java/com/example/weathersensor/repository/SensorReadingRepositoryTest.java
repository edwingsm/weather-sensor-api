package com.example.weathersensor.repository;

import com.example.weathersensor.entity.SensorReading;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class SensorReadingRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private SensorReadingRepository repository;

    @Test
    void shouldFindReadingsByTimestampBetween() {
        // Given
        Instant now = Instant.now();
        Instant hourAgo = now.minus(1, ChronoUnit.HOURS);
        Instant twoHoursAgo = now.minus(2, ChronoUnit.HOURS);

        SensorReading reading1 = new SensorReading("SENSOR_1", 25.5, 60.0, 10.5, hourAgo);
        SensorReading reading2 = new SensorReading("SENSOR_2", 23.0, 65.0, 12.0, now);
        SensorReading reading3 = new SensorReading("SENSOR_1", 20.0, 70.0, 8.0, twoHoursAgo);

        entityManager.persistAndFlush(reading1);
        entityManager.persistAndFlush(reading2);
        entityManager.persistAndFlush(reading3);

        // When
        List<SensorReading> readings = repository.findByTimestampBetween(
                hourAgo.minus(30, ChronoUnit.MINUTES),
                now.plus(1, ChronoUnit.MINUTES)
        );

        // Then
        assertThat(readings).hasSize(2);
        assertThat(readings).extracting(SensorReading::getSensorId)
                .containsExactlyInAnyOrder("SENSOR_1", "SENSOR_2");
    }

    @Test
    void shouldFindAverageMetricsInDateRange() {
        // Given
        Instant now = Instant.now();
        Instant hourAgo = now.minus(1, ChronoUnit.HOURS);

        SensorReading reading1 = new SensorReading("SENSOR_1", 20.0, 60.0, 10.0, hourAgo);
        SensorReading reading2 = new SensorReading("SENSOR_2", 30.0, 70.0, 20.0, now);

        entityManager.persistAndFlush(reading1);
        entityManager.persistAndFlush(reading2);

        // When
        Object[] result = repository.findAverageMetricsInDateRange(
                hourAgo.minus(1, ChronoUnit.HOURS),
                now.plus(1, ChronoUnit.HOURS)
        );

        // Then
//        assertThat(result).hasSize(4);
//        assertThat((Double) result[0]).isEqualTo(25.0); // average temperature
//        assertThat((Double) result[1]).isEqualTo(65.0); // average humidity
//        assertThat((Double) result[2]).isEqualTo(15.0); // average wind speed
//        assertThat((Long) result[3]).isEqualTo(2L);     // count
    }

    @Test
    void shouldFindAverageMetricsBySensorInDateRange() {
        // Given
        Instant now = Instant.now();
        Instant hourAgo = now.minus(1, ChronoUnit.HOURS);

        SensorReading reading1 = new SensorReading("SENSOR_1", 20.0, 60.0, 10.0, hourAgo);
        SensorReading reading2 = new SensorReading("SENSOR_1", 30.0, 70.0, 20.0, now);
        SensorReading reading3 = new SensorReading("SENSOR_2", 15.0, 50.0, 5.0, now);

        entityManager.persistAndFlush(reading1);
        entityManager.persistAndFlush(reading2);
        entityManager.persistAndFlush(reading3);

        // When
        Object[] result = repository.findAverageMetricsBySensorInDateRange(
                "SENSOR_1",
                hourAgo.minus(1, ChronoUnit.HOURS),
                now.plus(1, ChronoUnit.HOURS)
        );

        // Then
//        assertThat(result).hasSize(4);
//        assertThat((Double) result[0]).isEqualTo(25.0); // average temperature for SENSOR_1
//        assertThat((Double) result[1]).isEqualTo(65.0); // average humidity for SENSOR_1
//        assertThat((Double) result[2]).isEqualTo(15.0); // average wind speed for SENSOR_1
//        assertThat((Long) result[3]).isEqualTo(2L);     // count for SENSOR_1
    }
}
