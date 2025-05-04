package ru.hse.pensieve.profiles.routes;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import ru.hse.pensieve.database.cassandra.models.Profile;
import ru.hse.pensieve.profiles.models.ProfileRequest;
import ru.hse.pensieve.profiles.service.ProfileService;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.UUID;

@RestController
@RequestMapping("/profile")
public class ProfileController {

    @Autowired
    private ProfileService profileService;

    @PostMapping
    public ResponseEntity<?> createProfile(@ModelAttribute ProfileRequest request) {
        try {
            profileService.createProfile(request);
            return ResponseEntity.ok().build();
        } catch (IOException e) {
            return ResponseEntity.badRequest().body("Invalid avatar data");
        }
    }

    @PutMapping("/edit")
    public ResponseEntity<?> editProfile(@ModelAttribute ProfileRequest request) {
        try {
            profileService.editProfile(request);
            return ResponseEntity.ok().build();
        } catch (IOException e) {
            return ResponseEntity.badRequest().body("Invalid avatar data");
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/by-authorId")
    public ResponseEntity<Profile> getProfileByAuthorId(@RequestParam UUID authorId) {
        return ResponseEntity.ok(profileService.getProfileByAuthorId(authorId));
    }

    @GetMapping("/avatar")
    public ResponseEntity<ByteBuffer> getAvatarByAuthorId(@RequestParam UUID authorId) {
        return ResponseEntity.ok(profileService.getAvatarByAuthorId(authorId));
    }

    @GetMapping("/username")
    public ResponseEntity<String> getUsernameByAuthorId(@RequestParam UUID authorId) {
        return ResponseEntity.ok(profileService.getUsernameByAuthorId(authorId));
    }
}