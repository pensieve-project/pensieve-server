package ru.hse.pensieve.profiles.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.hse.pensieve.database.cassandra.models.Profile;
import ru.hse.pensieve.database.cassandra.repositories.*;
import ru.hse.pensieve.database.postgres.repositories.UserRepository;
import ru.hse.pensieve.profiles.models.BadAvatarException;
import ru.hse.pensieve.profiles.models.ProfileRequest;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Optional;
import java.util.UUID;

@Service
public class ProfileService {

    @Autowired
    private ProfileRepository profileRepository;

    @Autowired
    private UserRepository userRepository;

    public void createProfile(ProfileRequest request) throws BadAvatarException {
        byte[] avatarBytes;
        if (request.getAvatar() == null || request.getAvatar().isEmpty()) {
            throw new BadAvatarException("Avatar is null!");
        }
        try {
            avatarBytes = request.getAvatar().getBytes();
        } catch (IOException ex) {
            throw new BadAvatarException("Avatar is null!");
        }
        Profile profile = new Profile(request.getAuthorId(), ByteBuffer.wrap(avatarBytes), request.getDescription(), new ArrayList<>(), new ArrayList<>(), 0, 0, false);
        profileRepository.save(profile);
    }

    public void editProfile(ProfileRequest request) throws BadAvatarException {
        Profile profile = profileRepository.findByAuthorId(request.getAuthorId());
        byte[] avatarBytes;
        if (request.getAvatar() == null || request.getAvatar().isEmpty()) {
            throw new BadAvatarException("Avatar is null!");
        }
        try {
            avatarBytes = request.getAvatar().getBytes();
        } catch (IOException ex) {
            throw new BadAvatarException("Avatar is null!");
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
