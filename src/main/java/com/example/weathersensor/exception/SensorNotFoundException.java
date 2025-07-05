package com.example.weathersensor.exception;

import lombok.Getter;

public class SensorNotFoundException extends RuntimeException {

    public enum MODE {READING,SENSOR}
    @Getter
    MODE mode;

    public SensorNotFoundException(String message, MODE mode) {
        super(message);
        this.mode =mode;
    }

    public SensorNotFoundException(String message, Throwable cause, MODE mode) {
        super(message, cause);
        this.mode = mode;
    }
}
