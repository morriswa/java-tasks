package org.morriswa.taskapp.control;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HealthCheckController {
    @GetMapping(path = "health")
    public ResponseEntity<?> healthCheckup()
    {
        return ResponseEntity.ok("Hello! All is good on our side...");
    }
}
