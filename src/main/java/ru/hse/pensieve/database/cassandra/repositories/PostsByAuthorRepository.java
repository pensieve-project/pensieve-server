package ru.hse.pensieve.database.cassandra.repositories;

import org.springframework.data.cassandra.repository.CassandraRepository;
import ru.hse.pensieve.database.cassandra.models.PostsByAuthor;
import ru.hse.pensieve.database.cassandra.models.PostsByAuthorKey;

public interface PostsByAuthorRepository extends CassandraRepository<PostsByAuthor, PostsByAuthorKey> {
}
