package com.example.weathersensor.exception;

public class SensorAlreadyExistsException extends RuntimeException {

    public SensorAlreadyExistsException(String message) {
        super(message);
    }

    public SensorAlreadyExistsException(String message, Throwable cause) {
        super(message, cause);
    }
}
