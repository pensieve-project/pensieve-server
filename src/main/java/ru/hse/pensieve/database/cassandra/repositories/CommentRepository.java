package ru.hse.pensieve.database.cassandra.repositories;

import org.springframework.data.cassandra.repository.CassandraRepository;
import ru.hse.pensieve.database.cassandra.models.Comment;
import ru.hse.pensieve.database.cassandra.models.CommentKey;

import java.util.List;
import java.util.UUID;

public interface CommentRepository extends CassandraRepository<Comment, CommentKey> {
    List<Comment> findByKeyPostId(UUID postId);
}
