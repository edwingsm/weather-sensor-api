package com.example.weathersensor.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.servers.Server;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "Weather Sensor API",
                version = "1.0",
                description = "REST API for managing weather sensor readings and calculating metrics"
        ),
        servers = @Server(url = "http://localhost:8080", description = "Development server")
)
public class OpenApiConfig {
}