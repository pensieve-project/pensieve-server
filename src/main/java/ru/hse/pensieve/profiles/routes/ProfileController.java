package ru.hse.pensieve.profiles.routes;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.hse.pensieve.profiles.models.ProfileRequest;
import ru.hse.pensieve.profiles.service.ProfileService;

import java.io.IOException;

@RestController
@RequestMapping("/profile")
public class ProfileController {

    @Autowired
    private ProfileService profileService;

    @PostMapping("/create")
    public ResponseEntity<String> createProfile(@RequestBody ProfileRequest request) {
        try {
            profileService.createProfile(request);
            return ResponseEntity.ok().build();
        } catch (IOException e) {
            return ResponseEntity.badRequest().body("Invalid avatar data");
        }
    }

    @PutMapping("/edit")
    public ResponseEntity<String> editProfile(@RequestBody ProfileRequest request) {
        try {
            profileService.editProfile(request);
            return ResponseEntity.ok().build();
        } catch (IOException e) {
            return ResponseEntity.badRequest().body("Invalid avatar data");
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
}