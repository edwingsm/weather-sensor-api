package com.example.weathersensor.exception;

public class SensorNotFoundException extends RuntimeException {

    public SensorNotFoundException(String message) {
        super(message);
    }

    public SensorNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
