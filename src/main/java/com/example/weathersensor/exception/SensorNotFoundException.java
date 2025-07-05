package com.example.weathersensor.exception;

import lombok.Getter;

public class SensorNotFoundException extends RuntimeException {

    // This exception can thrown ,
    // - while querying details about sensor
    // - while querying adding a sensor reading
    // so thought better to know when exception is thrown to give proper error
    // message
    public enum MODE {
        READING, SENSOR
    }

    @Getter
    MODE mode;

    public SensorNotFoundException(String message, MODE mode) {
        super(message);
        this.mode = mode;
    }

    public SensorNotFoundException(String message, Throwable cause, MODE mode) {
        super(message, cause);
        this.mode = mode;
    }
}
