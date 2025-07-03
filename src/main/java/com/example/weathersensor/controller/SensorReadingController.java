package com.example.weathersensor.controller;

import com.example.weathersensor.dto.AverageMetricResponse;
import com.example.weathersensor.dto.SensorReadingRequest;
import com.example.weathersensor.dto.SensorReadingResponse;
import com.example.weathersensor.service.SensorReadingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.OffsetDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/v1/sensor-readings")
@Tag(name = "Sensor Readings", description = "API for managing weather sensor readings")
public class SensorReadingController {

    private final SensorReadingService service;

    @Autowired
    public SensorReadingController(SensorReadingService service) {
        this.service = service;
    }

    @PostMapping
    @Operation(summary = "Register a new sensor reading",
            description = "Register weather metrics from a sensor. Timestamp is accepted in any timezone and stored as UTC.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Sensor reading registered successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input data")
    })
    public ResponseEntity<SensorReadingResponse> registerReading(
            @Valid @RequestBody SensorReadingRequest request) {

        SensorReadingResponse response = service.registerReading(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/average")
    @Operation(summary = "Get average metrics for all sensors",
            description = "Calculate average temperature, humidity, and wind speed for all sensors in a date range")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Average metrics calculated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid date range")
    })
    public ResponseEntity<AverageMetricResponse> getAverageMetrics(
            @Parameter(description = "Start date and time (ISO 8601 format with timezone)")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime startTime,

            @Parameter(description = "End date and time (ISO 8601 format with timezone)")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime endTime) {

        AverageMetricResponse response = service.getAverageMetrics(startTime, endTime);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/average/{sensorId}")
    @Operation(summary = "Get average metrics for a specific sensor",
            description = "Calculate average temperature, humidity, and wind speed for a specific sensor in a date range")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Average metrics calculated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid date range or sensor ID")
    })
    public ResponseEntity<AverageMetricResponse> getAverageMetricsBySensor(
            @Parameter(description = "Unique sensor identifier")
            @PathVariable String sensorId,

            @Parameter(description = "Start date and time (ISO 8601 format with timezone)")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime startTime,

            @Parameter(description = "End date and time (ISO 8601 format with timezone)")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime endTime) {

        AverageMetricResponse response = service.getAverageMetricsBySensor(sensorId, startTime, endTime);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    @Operation(summary = "Get all sensor readings in a date range",
            description = "Retrieve all sensor readings within a specified date range")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Sensor readings retrieved successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid date range")
    })
    public ResponseEntity<List<SensorReadingResponse>> getReadings(
            @Parameter(description = "Start date and time (ISO 8601 format with timezone)")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime startTime,

            @Parameter(description = "End date and time (ISO 8601 format with timezone)")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime endTime) {

        List<SensorReadingResponse> readings = service.getReadings(startTime, endTime);
        return ResponseEntity.ok(readings);
    }
}