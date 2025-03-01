package ru.hse.pensieve.authentication.route;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpServletResponse;

import ru.hse.pensieve.authentication.model.*;
import ru.hse.pensieve.authentication.service.AuthenticationService;

@RestController
@RequestMapping("/auth")
public class AuthenticationController {

    @Autowired
    private AuthenticationService authenticationService;

    @PostMapping("/register")
    public ResponseEntity<AuthenticationResponse> registerUser(@RequestBody RegisterRequest request, HttpServletResponse response) {
        return ResponseEntity.ok(authenticationService.register(request, response).join());
    }

    @PostMapping("/login")
    public ResponseEntity<AuthenticationResponse> loginUser(@RequestBody AuthenticationRequest request, HttpServletResponse response) {
        return ResponseEntity.ok(authenticationService.login(request, response).join());
    }

    @GetMapping("/token")
    public ResponseEntity<Tokens> getNewTokens(@CookieValue(value = "refresh_token", required = false) String refreshToken, HttpServletResponse response) {
        if (refreshToken == null) {
            return ResponseEntity.status(400).body(null);
        }
        return ResponseEntity.ok(authenticationService.getNewTokens(refreshToken, response));
    }
}
