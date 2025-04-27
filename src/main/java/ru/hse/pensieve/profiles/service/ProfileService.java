package ru.hse.pensieve.profiles.service;

import ru.hse.pensieve.database.cassandra.models.Profile;
import ru.hse.pensieve.profiles.models.BadAvatarException;
import ru.hse.pensieve.profiles.models.ProfileRequest;

import java.nio.ByteBuffer;
import java.util.UUID;

public interface ProfileService {

    void createProfile(ProfileRequest request) throws BadAvatarException;

    void editProfile(ProfileRequest request) throws BadAvatarException;

    Profile getProfileByAuthorId(UUID authorId);

    ByteBuffer getAvatarByAuthorId(UUID authorId);

    String getUsernameByAuthorId(UUID authorId);
}
