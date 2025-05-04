package ru.hse.pensieve.profiles.routes;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import ru.hse.pensieve.config.exceptions.ErrorResponse;
import ru.hse.pensieve.database.cassandra.models.Profile;
import ru.hse.pensieve.posts.models.BadPostException;
import ru.hse.pensieve.profiles.models.BadAvatarException;
import ru.hse.pensieve.profiles.models.ProfileRequest;
import ru.hse.pensieve.profiles.service.ProfileService;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/profile")
public class ProfileController {

    @Autowired
    private ProfileService profileService;

    @PostMapping
    public ResponseEntity<?> createProfile(@ModelAttribute ProfileRequest request) {
        profileService.createProfile(request);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/edit")
    public ResponseEntity<?> editProfile(@ModelAttribute ProfileRequest request) {
        profileService.editProfile(request);
        return ResponseEntity.ok().build();
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

    @ExceptionHandler(BadAvatarException.class)
    public ResponseEntity<ErrorResponse> handleBadAvatarException(Exception ex) {
        log.error("Avatar is null: ", ex);

        ErrorResponse error = new ErrorResponse(
                "Avatar is null: " + ex.getMessage(),
                HttpStatus.BAD_REQUEST.value()
        );
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }
}