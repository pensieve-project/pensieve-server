package ru.hse.pensieve;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
public class UserController {
    private final UserRepository userRepository;

    public UserController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @PostMapping("/register")
    public ResponseEntity<Map<String, String>> registerUser(User user) {
        Map<String, String> response = new HashMap<>();
        if (userRepository.existsByUsername(user.getUsername())) {
            response.put("message", "The user is already registered!");
            return ResponseEntity.badRequest().body(response);
        }
        userRepository.save(user);
        response.put("message", "User successfully registered");
        return ResponseEntity.ok(response);
    }
}
