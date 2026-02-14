package com.example.validatingforminput;

import com.example.validatingforminput.service.RegistrationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/validate")
public class ValidationApiController {

    private final RegistrationService registrationService;

    public ValidationApiController(RegistrationService registrationService) {
        this.registrationService = registrationService;
    }

    @GetMapping("/email")
    public ResponseEntity<Map<String, Object>> validateEmail(@RequestParam String email) {
        Map<String, Object> result = new HashMap<>();
        boolean valid = email != null && email.matches("^[\\w.+-]+@[\\w.-]+\\.[a-zA-Z]{2,}$");
        boolean available = !registrationService.emailExists(email);

        result.put("valid", valid);
        result.put("available", available);
        if (!valid) {
            result.put("message", "Invalid email format");
        } else if (!available) {
            result.put("message", "This email is already registered");
        } else {
            result.put("message", "Email is available");
        }
        return ResponseEntity.ok(result);
    }

    @GetMapping("/name")
    public ResponseEntity<Map<String, Object>> validateName(@RequestParam String name) {
        Map<String, Object> result = new HashMap<>();
        boolean valid = name != null && name.length() >= 2 && name.length() <= 30;
        result.put("valid", valid);
        result.put("message", valid ? "Looks good" : "Name must be 2-30 characters");
        return ResponseEntity.ok(result);
    }

    @GetMapping("/phone")
    public ResponseEntity<Map<String, Object>> validatePhone(@RequestParam String phone) {
        Map<String, Object> result = new HashMap<>();
        if (phone == null || phone.isBlank()) {
            result.put("valid", true);
            result.put("message", "Optional field");
        } else {
            boolean valid = phone.matches("^\\+?[0-9\\-\\s()]{7,20}$");
            result.put("valid", valid);
            result.put("message", valid ? "Valid phone number" : "Invalid format (e.g. +1-555-123-4567)");
        }
        return ResponseEntity.ok(result);
    }

    @GetMapping("/password")
    public ResponseEntity<Map<String, Object>> validatePassword(@RequestParam String password) {
        Map<String, Object> result = new HashMap<>();
        boolean hasLength = password != null && password.length() >= 8;
        boolean hasUpper = password != null && password.matches(".*[A-Z].*");
        boolean hasLower = password != null && password.matches(".*[a-z].*");
        boolean hasDigit = password != null && password.matches(".*\\d.*");

        result.put("valid", hasLength && hasUpper && hasLower && hasDigit);
        result.put("hasLength", hasLength);
        result.put("hasUpper", hasUpper);
        result.put("hasLower", hasLower);
        result.put("hasDigit", hasDigit);
        return ResponseEntity.ok(result);
    }
}
