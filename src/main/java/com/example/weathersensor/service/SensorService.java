package com.example.weathersensor.service;


import com.example.weathersensor.dto.SensorRegistrationDto;
import com.example.weathersensor.dto.SensorResponseDto;
import com.example.weathersensor.entity.Sensor;
import com.example.weathersensor.repository.SensorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
public class SensorService {

    @Autowired
    private SensorRepository sensorRepository;

    /**
     * Register a new sensor
     */
    public SensorResponseDto registerSensor(SensorRegistrationDto dto) {
        Sensor sensor = new Sensor(dto.tag(),dto.location(), dto.timeZone());
        Sensor savedSensor = sensorRepository.save(sensor);

        return new SensorResponseDto(
                savedSensor.getId(),
                savedSensor.getTag(),
                savedSensor.getLocation(),
                savedSensor.getTimeZone()
        );
    }

    /**
     * Get all sensors
     */
    public List<SensorResponseDto> getAllSensors() {
        return sensorRepository.findAll().stream()
                .map(sensor -> new SensorResponseDto(
                        sensor.getId(),
                        sensor.getTag(),
                        sensor.getLocation(),
                        sensor.getTimeZone()
                ))
                .toList();
    }

    /**
     * Get sensor by ID
     */
    public Optional<Sensor> getSensorById(Long id) {
        return sensorRepository.findById(id);
    }

    /**
     * Get sensor by ID
     */
    public Optional<Sensor> getSensorByTag(String tag) {
        return sensorRepository.findByTag(tag);
    }

    /**
     * Get sensors by location
     */
    public List<SensorResponseDto> getSensorsByLocation(String location) {
        return sensorRepository.findByLocation(location).stream()
                .map(sensor -> new SensorResponseDto(
                        sensor.getId(),
                        sensor.getTag(),
                        sensor.getLocation(),
                        sensor.getTimeZone()
                ))
                .toList();
    }
}