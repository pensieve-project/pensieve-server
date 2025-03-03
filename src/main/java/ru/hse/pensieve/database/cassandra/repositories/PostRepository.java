package ru.hse.pensieve.database.cassandra.repositories;

import org.springframework.data.cassandra.repository.CassandraRepository;
import ru.hse.pensieve.database.cassandra.models.Post;
import ru.hse.pensieve.database.cassandra.models.PostKey;

import java.util.List;
import java.util.UUID;

public interface PostRepository extends CassandraRepository<Post, PostKey> {
    List<Post> findByKeyThemeId(UUID themeId);
}
