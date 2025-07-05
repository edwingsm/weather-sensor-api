package com.example.weathersensor.repository;

import com.example.weathersensor.entity.Sensor;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
@ActiveProfiles("test")
class SensorRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private SensorRepository sensorRepository;

    private Sensor berlinSensor1;
    private Sensor berlinSensor2;
    private Sensor londonSensor;

    @BeforeEach
    void setUp() {
        berlinSensor1 = new Sensor("SENSOR_001", "Berlin", "Europe/Berlin");
        berlinSensor2 = new Sensor("SENSOR_002", "Berlin", "Europe/Berlin");
        londonSensor = new Sensor("SENSOR_003", "London", "Europe/London");

        entityManager.persistAndFlush(berlinSensor1);
        entityManager.persistAndFlush(berlinSensor2);
        entityManager.persistAndFlush(londonSensor);
    }

    @Test
    void shouldReturnTrueIfSensorTagExists() {
        assertThat(sensorRepository.existsByTag("SENSOR_001")).isTrue();
    }

    @Test
    void shouldReturnFalseIfSensorTagDoesNotExist() {
        assertThat(sensorRepository.existsByTag("UNKNOWN_TAG")).isFalse();
    }

    @Test
    void shouldFindSensorByTag() {
        Optional<Sensor> found = sensorRepository.findByTag("SENSOR_002");
        assertThat(found).isPresent();
        assertThat(found.get().getLocation()).isEqualTo("Berlin");
    }

    @Test
    void shouldNotFindSensorByUnknownTag() {
        Optional<Sensor> result = sensorRepository.findByTag("NON_EXISTENT");
        assertThat(result).isNotPresent();
    }

    @Test
    void shouldFindSensorsByLocation() {
        List<Sensor> berlinSensors = sensorRepository.findByLocation("Berlin");
        assertThat(berlinSensors).hasSize(2);
    }

    @Test
    void shouldReturnEmptyListIfNoSensorsInLocation() {
        List<Sensor> dublinSensors = sensorRepository.findByLocation("Dublin");
        assertThat(dublinSensors).isEmpty();
    }

    @Test
    void shouldBeCaseSensitiveInLocationQuery() {
        List<Sensor> result = sensorRepository.findByLocation("berlin"); // lowercase
        assertThat(result).isEmpty(); // assumes case-sensitive DB collation
    }

    @Test
    void shouldSaveNewSensor() {
        Sensor newSensor = new Sensor("SENSOR_004", "Madrid", "Europe/Madrid");
        Sensor saved = sensorRepository.save(newSensor);

        Optional<Sensor> result = sensorRepository.findByTag("SENSOR_004");
        assertThat(result).isPresent();
        assertThat(result.get().getLocation()).isEqualTo("Madrid");
    }

    @Test
    void shouldPreventDuplicateTagIfUniqueConstraintApplied() {
        Sensor duplicate = new Sensor("SENSOR_001", "Paris", "Europe/Paris");

        // This assumes tag is unique. If not, this test should be removed or adjusted.
        assertThatThrownBy(() -> {
            sensorRepository.saveAndFlush(duplicate);
        }).isInstanceOf(Exception.class); // usually DataIntegrityViolationException
    }

    @Test
    void shouldHandleNullTagGracefully() {
        Sensor sensorWithNullTag = new Sensor(null, "Rome", "Europe/Rome");
        assertThatThrownBy(() -> {
            sensorRepository.saveAndFlush(sensorWithNullTag);
        }).isInstanceOf(Exception.class);
    }
}