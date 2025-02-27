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

    @ResponseBody
    @PostMapping("/register")
    public ResponseEntity<AuthenticationResponse> registerUser(@RequestBody RegisterRequest request) {
        return ResponseEntity.ok(authenticationService.register(request).join());
    }

    @ResponseBody
    @PostMapping("/login")
    public ResponseEntity<AuthenticationResponse> loginUser(@RequestBody AuthenticationRequest request) {
        return ResponseEntity.ok(authenticationService.login(request).join());
    }

    @ResponseBody
    @GetMapping("/token")
    public ResponseEntity<Tokens> getNewTokens(@RequestBody RefreshRequest request) {
        return ResponseEntity.ok(authenticationService.getNewTokens(request));
    }
}
