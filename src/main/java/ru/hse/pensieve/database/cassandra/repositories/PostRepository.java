package ru.hse.pensieve.database.cassandra.repositories;

import org.springframework.data.cassandra.repository.CassandraRepository;
import ru.hse.pensieve.database.cassandra.models.Post;
import ru.hse.pensieve.database.cassandra.models.PostKey;

public interface PostRepository extends CassandraRepository<Post, PostKey> {
}
