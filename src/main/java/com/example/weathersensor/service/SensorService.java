package com.example.weathersensor.service;

import com.example.weathersensor.dto.SensorRegistrationRequest;
import com.example.weathersensor.dto.SensorResponseDto;
import com.example.weathersensor.entity.Sensor;
import com.example.weathersensor.exception.SensorAlreadyExistsException;
import com.example.weathersensor.repository.SensorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
public class SensorService {

    @Autowired
    private SensorRepository sensorRepository;

    public SensorResponseDto registerSensor(SensorRegistrationRequest dto) {

        if (sensorRepository.existsByTag(dto.tag())) {
            throw new SensorAlreadyExistsException("Sensor with tag '" + dto.tag() + "' already exists");
        }

        Sensor sensor = new Sensor(dto.tag(), dto.location(), dto.timeZone());
        Sensor savedSensor = sensorRepository.save(sensor);

        return new SensorResponseDto(
                savedSensor.getId(),
                savedSensor.getTag(),
                savedSensor.getLocation(),
                savedSensor.getTimeZone());
    }

    public List<SensorResponseDto> getAllSensors() {
        return sensorRepository.findAll().stream()
                .map(sensor -> new SensorResponseDto(
                        sensor.getId(),
                        sensor.getTag(),
                        sensor.getLocation(),
                        sensor.getTimeZone()))
                .toList();
    }

    public Optional<Sensor> getSensorById(Long id) {
        return sensorRepository.findById(id);
    }

    public Optional<Sensor> getSensorByTag(String tag) {
        return sensorRepository.findByTag(tag);
    }

    public List<SensorResponseDto> getSensorsByLocation(String location) {
        return sensorRepository.findByLocation(location).stream()
                .map(sensor -> new SensorResponseDto(
                        sensor.getId(),
                        sensor.getTag(),
                        sensor.getLocation(),
                        sensor.getTimeZone()))
                .toList();
    }
}