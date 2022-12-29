package org.morriswa.taskapp.control;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
public class HealthCheckController {
    @GetMapping(path = "health")
    public ResponseEntity<?> healthCheckup()
    {
        Map<String, Object> response = new HashMap<>(){{
            put("message","Hello! All is good on our side...");
        }};
        return ResponseEntity.ok().body(response);
    }
}
