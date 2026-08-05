package com.kolaysoft.projecttracking.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@Tag(
        name = "Health",
        description = "Uygulamanın çalışma durumunu kontrol eden endpointler"
)
@RestController
@RequestMapping("/api/health")
public class HealthController {

    @Operation(
            summary = "Backend sağlık kontrolü",
            description = "Backend uygulamasının çalışıp çalışmadığını kontrol eder."
    )
    @GetMapping
    public Map<String, String> checkHealth() {
        return Map.of(
                "status", "UP",
                "message", "Project Tracking Backend is running"
        );
    }
}