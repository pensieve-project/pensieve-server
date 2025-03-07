package ru.hse.pensieve.database.cassandra.repositories;

import org.springframework.data.cassandra.repository.CassandraRepository;
import ru.hse.pensieve.database.cassandra.models.PostById;
import ru.hse.pensieve.database.cassandra.models.PostByIdKey;

import java.util.List;
import java.util.UUID;

public interface PostByIdRepository extends CassandraRepository<PostById, PostByIdKey> {
    List<PostById> findByKeyPostId(UUID postId);
}
