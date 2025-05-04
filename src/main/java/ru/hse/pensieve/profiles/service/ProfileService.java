package ru.hse.pensieve.profiles.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.hse.pensieve.database.cassandra.models.Profile;
import ru.hse.pensieve.database.cassandra.repositories.*;
import ru.hse.pensieve.database.postgres.repositories.UserRepository;
import ru.hse.pensieve.profiles.models.ProfileRequest;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.UUID;

@Service
public class ProfileService {

    @Autowired
    private ProfileRepository profileRepository;

    @Autowired
    private UserRepository userRepository;

    public void createProfile(ProfileRequest request) throws IOException {
        byte[] avatarBytes = (request.getAvatar() != null && !request.getAvatar().isEmpty()) ? request.getAvatar().getBytes() : null;
        if (avatarBytes == null) {
            throw new IOException();
        }
        Profile profile = new Profile(request.getAuthorId(), ByteBuffer.wrap(avatarBytes), request.getDescription(), new ArrayList<>(), new ArrayList<>());
        profileRepository.save(profile);
    }

    public void editProfile(ProfileRequest request) throws IOException {
        Profile profile = profileRepository.findByAuthorId(request.getAuthorId());
        byte[] avatarBytes = (request.getAvatar() != null && !request.getAvatar().isEmpty()) ? request.getAvatar().getBytes() : null;
        if (avatarBytes == null) {
            throw new IOException();
        }
        profile.setAvatar(ByteBuffer.wrap(avatarBytes));
        profile.setDescription(request.getDescription());
        profileRepository.save(profile);
    }

    public Profile getProfileByAuthorId(UUID authorId) {
        return profileRepository.findByAuthorId(authorId);
    }

    public ByteBuffer getAvatarByAuthorId(UUID authorId) {
        return getProfileByAuthorId(authorId).getAvatar();
    }

    public String getUsernameByAuthorId(UUID authorId) {
        return userRepository.findUsernameById(authorId);
    }
}
