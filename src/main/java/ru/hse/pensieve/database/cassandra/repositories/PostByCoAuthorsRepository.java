package ru.hse.pensieve.database.cassandra.repositories;

import org.springframework.data.cassandra.repository.CassandraRepository;
import ru.hse.pensieve.database.cassandra.models.PostByCoAuthors;
import ru.hse.pensieve.database.cassandra.models.PostByCoAuthorsKey;

import java.util.List;
import java.util.Set;
import java.util.UUID;

public interface PostByCoAuthorsRepository extends CassandraRepository<PostByCoAuthors, PostByCoAuthorsKey> {
    List<PostByCoAuthors> findByKeyCoAuthors(Set<UUID> coAuthors);
}
