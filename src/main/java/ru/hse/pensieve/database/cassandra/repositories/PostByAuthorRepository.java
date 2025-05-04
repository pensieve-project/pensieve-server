package ru.hse.pensieve.database.cassandra.repositories;

import org.springframework.data.cassandra.repository.CassandraRepository;
import org.springframework.data.cassandra.repository.Query;
import ru.hse.pensieve.database.cassandra.models.PostByAuthor;
import ru.hse.pensieve.database.cassandra.models.PostByAuthorKey;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface PostByAuthorRepository extends CassandraRepository<PostByAuthor, PostByAuthorKey> {
    List<PostByAuthor> findByKeyAuthorId(UUID authorId);

    @Query("SELECT * FROM posts_by_author WHERE authorId = ?0 LIMIT ?1")
    List<PostByAuthor> findRecentByAuthor(UUID authorId, int limit);

    @Query("SELECT * FROM posts_by_author WHERE authorId = ?0 AND timeStamp < ?1 LIMIT ?2")
    List<PostByAuthor> findRecentByAuthor(UUID authorId, Instant before, int limit);
}
