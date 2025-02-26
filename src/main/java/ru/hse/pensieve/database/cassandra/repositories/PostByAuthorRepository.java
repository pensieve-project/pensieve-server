package ru.hse.pensieve.database.cassandra.repositories;

import org.springframework.data.cassandra.repository.CassandraRepository;
import ru.hse.pensieve.database.cassandra.models.PostByAuthor;
import ru.hse.pensieve.database.cassandra.models.PostByAuthorKey;

import java.util.List;
import java.util.UUID;

public interface PostByAuthorRepository extends CassandraRepository<PostByAuthor, PostByAuthorKey> {
    List<PostByAuthor> findByKeyAuthorId(UUID authorId);
}
