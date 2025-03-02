package ru.hse.pensieve.authentication.route;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import ru.hse.pensieve.authentication.model.*;
import ru.hse.pensieve.authentication.service.AuthenticationService;

@RestController
@RequestMapping("/auth")
public class AuthenticationController {

    @Autowired
    private AuthenticationService authenticationService;

    @PostMapping("/register")
    public ResponseEntity<AuthenticationResponse> registerUser(@RequestBody RegisterRequest request) {
        return ResponseEntity.ok(authenticationService.register(request).join());
    }

    @PostMapping("/login")
    public ResponseEntity<AuthenticationResponse> loginUser(@RequestBody AuthenticationRequest request) {
        return ResponseEntity.ok(authenticationService.login(request).join());
    }

    @GetMapping("/token")
    public ResponseEntity<Tokens> getNewTokens(String refreshToken) {
        if (refreshToken == null) {
            return ResponseEntity.status(400).body(null);
        }
        Tokens newTokens = authenticationService.getNewTokens(refreshToken);
        return ResponseEntity.ok(newTokens);
    }
}
