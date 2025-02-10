package ru.hse.pensieve;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
public class Controller {

    @GetMapping("/")
    public ResponseEntity<Map<String, String>> greeting() {
        Map<String, String> response = new HashMap<>();
        response.put("message", "Greetings from Spring Boot!");
        return ResponseEntity.ok().body(response);
    }
}
