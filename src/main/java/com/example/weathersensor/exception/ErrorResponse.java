package com.example.weathersensor.exception;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Error response structure")
public class ErrorResponse {

    @Schema(description = "Error message", example = "Invalid input data")
    private String message;

    @Schema(description = "HTTP status code", example = "400")
    private int status;

    @Schema(description = "Timestamp of the error", example = "2024-01-15T10:30:00Z")
    private String timestamp;

    @Schema(description = "Request path where error occurred", example = "/api/sensors")
    private String path;

    // Constructors
    public ErrorResponse() {
    }

    public ErrorResponse(String message, int status, String timestamp, String path) {
        this.message = message;
        this.status = status;
        this.timestamp = timestamp;
        this.path = path;
    }

    // Getters and Setters
    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }
}