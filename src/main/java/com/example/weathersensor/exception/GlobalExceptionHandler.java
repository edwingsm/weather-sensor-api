package com.example.weathersensor.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.validation.FieldError;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler {

        /**
         * Handle validation errors (e.g., @Valid annotations)
         */
        @ExceptionHandler(MethodArgumentNotValidException.class)
        public ResponseEntity<ErrorResponse> handleValidationExceptions(
                        MethodArgumentNotValidException ex, WebRequest request) {

                Map<String, String> validationErrors = new HashMap<>();
                ex.getBindingResult().getAllErrors().forEach((error) -> {
                        String fieldName = ((FieldError) error).getField();
                        String errorMessage = error.getDefaultMessage();
                        validationErrors.put(fieldName, errorMessage);
                });

                ErrorResponse errorResponse = new ErrorResponse(
                        "Validation failed",
                        HttpStatus.BAD_REQUEST.value(),
                        LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
                        request.getDescription(false).replace("uri=", "")
                );

                return ResponseEntity.badRequest().body(errorResponse);
        }

        /**
         * Handle custom SensorAlreadyExistsException
         */
        @ExceptionHandler(SensorAlreadyExistsException.class)
        public ResponseEntity<ErrorResponse> handleSensorAlreadyExists(
                        SensorAlreadyExistsException ex, WebRequest request) {

                ErrorResponse errorResponse = new ErrorResponse(
                                ex.getMessage(),
                                HttpStatus.CONFLICT.value(),
                                LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
                                request.getDescription(false).replace("uri=", ""));

                return ResponseEntity.status(HttpStatus.CONFLICT).body(errorResponse);
        }

        /**
         * Handle custom SensorNotFoundException
         */
        @ExceptionHandler(SensorNotFoundException.class)
        public ResponseEntity<ErrorResponse> handleSensorNotFound(
                        SensorNotFoundException ex, WebRequest request) {

                HttpStatus httpStatus = ex.getMode().equals(SensorNotFoundException.MODE.SENSOR) ? HttpStatus.NOT_FOUND
                                : HttpStatus.UNPROCESSABLE_ENTITY;

                ErrorResponse errorResponse = new ErrorResponse(
                                ex.getMessage(),
                                httpStatus.value(),
                                LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
                                request.getDescription(false).replace("uri=", ""));

                return ResponseEntity.status(httpStatus).body(errorResponse);
        }

        @ExceptionHandler(HttpMessageNotReadableException.class)
        public ResponseEntity<ErrorResponse> handleInvalidJson(HttpMessageNotReadableException ex, WebRequest request) {
                ErrorResponse errorResponse = new ErrorResponse(
                        "Malformed JSON request: " + ex.getMostSpecificCause().getMessage(),
                        HttpStatus.BAD_REQUEST.value(),
                        LocalDateTime.now().atOffset(ZoneOffset.UTC).format(DateTimeFormatter.ISO_OFFSET_DATE_TIME),
                        request.getDescription(false).replace("uri=", "")
                );
                return ResponseEntity.badRequest().body(errorResponse);
        }

        /**
         * Handle IllegalArgumentException
         */
        @ExceptionHandler(IllegalArgumentException.class)
        public ResponseEntity<ErrorResponse> handleIllegalArgument(
                        IllegalArgumentException ex, WebRequest request) {

                ErrorResponse errorResponse = new ErrorResponse(
                                ex.getMessage(),
                                HttpStatus.BAD_REQUEST.value(),
                                LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
                                request.getDescription(false).replace("uri=", ""));

                return ResponseEntity.badRequest().body(errorResponse);
        }

        /**
         * Handle all other exceptions
         */
        @ExceptionHandler(Exception.class)
        public ResponseEntity<ErrorResponse> handleGlobalException(
                        Exception ex, WebRequest request) {

                ErrorResponse errorResponse = new ErrorResponse(
                                "An unexpected error occurred: " + ex.getMessage(),
                                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                                LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
                                request.getDescription(false).replace("uri=", ""));

                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
}
