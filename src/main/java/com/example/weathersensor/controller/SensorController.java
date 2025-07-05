package com.example.weathersensor.controller;

import com.example.weathersensor.dto.SensorRegistrationRequest;
import com.example.weathersensor.dto.SensorResponseDto;
import com.example.weathersensor.exception.ErrorResponse;
import com.example.weathersensor.service.SensorService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/sensors")
@Tag(name = "Sensor Management", description = "API for managing sensors including registration, retrieval, and location-based queries")
public class SensorController {

        @Autowired
        private SensorService sensorService;

        @PostMapping
        @Operation(summary = "Register a new sensor", description = "Creates and registers a new sensor in the system with the provided details")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "201", description = "Sensor successfully registered", content = @Content(mediaType = "application/json", schema = @Schema(implementation = SensorResponseDto.class))),
                        @ApiResponse(responseCode = "400", description = "Invalid input data provided", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
                        @ApiResponse(responseCode = "409", description = "Sensor with the same identifier already exists", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
        })
        public ResponseEntity<SensorResponseDto> registerSensor(
                        @Parameter(description = "Sensor registration details", required = true) @Valid @RequestBody SensorRegistrationRequest dto) {
                SensorResponseDto response = sensorService.registerSensor(dto);
                return ResponseEntity.status(HttpStatus.CREATED).body(response);
        }

        @GetMapping
        @Operation(summary = "Get all sensors", description = "Retrieves a list of all sensors registered in the system")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Successfully retrieved all sensors", content = @Content(mediaType = "application/json", schema = @Schema(implementation = SensorResponseDto.class))),
                        @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
        })
        public ResponseEntity<List<SensorResponseDto>> getAllSensors() {
                List<SensorResponseDto> sensors = sensorService.getAllSensors();
                return ResponseEntity.ok(sensors);
        }

        @GetMapping("/location/{location}")
        @Operation(summary = "Get sensors by location", description = "Retrieves all sensors that are located in the specified location")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Successfully retrieved sensors for the specified location", content = @Content(mediaType = "application/json", schema = @Schema(implementation = SensorResponseDto.class))),
                        @ApiResponse(responseCode = "404", description = "No sensors found for the specified location", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
                        @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
        })
        public ResponseEntity<List<SensorResponseDto>> getSensorsByLocation(
                        @Parameter(description = "Location to filter sensors by", required = true, example = "warehouse-a") @PathVariable String location) {
                List<SensorResponseDto> sensors = sensorService.getSensorsByLocation(location);
                return ResponseEntity.ok(sensors);
        }
}