package ru.hse.pensieve.database.cassandra.repositories;

import org.springframework.data.cassandra.repository.CassandraRepository;
import ru.hse.pensieve.database.cassandra.models.Profile;

import java.util.UUID;

public interface ProfileRepository extends CassandraRepository<Profile, UUID> {
    Profile findByAuthorId(UUID authorId);

    default boolean hasLikedPost(UUID authorId, UUID postId) {
        Profile profile = findByAuthorId(authorId);
        if (profile != null && profile.getLikedPostsIds() != null) {
            return profile.getLikedPostsIds().contains(postId);
        }
        return false;
    }

    default boolean hasLikedTheme(UUID authorId, UUID themeId) {
        Profile profile = findByAuthorId(authorId);
        if (profile != null && profile.getLikedThemesIds() != null) {
            return profile.getLikedThemesIds().contains(themeId);
        }
        return false;
    }
}
