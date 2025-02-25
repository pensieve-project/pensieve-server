package ru.hse.pensieve.database.cassandra.repositories;

import java.util.List;
import java.util.UUID;

import org.springframework.data.repository.CrudRepository;
import ru.hse.pensieve.database.cassandra.models.Post;

public interface PostRepository extends CrudRepository<Post, UUID> {
    List<Post> findByAuthorId(UUID authorId);
    List<Post> findByThreadId(UUID threadId);
}
