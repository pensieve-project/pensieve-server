package ru.hse.pensieve.authorization;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import ru.hse.pensieve.authorization.model.AuthorizationRequest;
import ru.hse.pensieve.authorization.model.AuthenticationResponse;
import ru.hse.pensieve.authorization.model.RegisterRequest;
import ru.hse.pensieve.authorization.service.AuthenticationService;

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
    public ResponseEntity<AuthenticationResponse> loginUser(@RequestBody AuthorizationRequest request) {
        return ResponseEntity.ok(authenticationService.login(request).join());
    }
}
