package ru.hse.pensieve.users.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.hse.pensieve.database.cassandra.models.Profile;
import ru.hse.pensieve.database.cassandra.repositories.*;
import ru.hse.pensieve.database.postgres.models.User;

import java.util.ArrayList;
import java.util.UUID;

@Service
public class ProfileService {

    @Autowired
    private ProfileRepository profileRepository;

    public void createProfile(User user) { // !!
        Profile profile = new Profile(user.getId(), "", new ArrayList<>());
        profileRepository.save(profile);
    }
}
