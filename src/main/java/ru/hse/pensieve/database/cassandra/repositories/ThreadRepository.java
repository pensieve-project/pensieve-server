package ru.hse.pensieve.database.cassandra.repositories;

import org.springframework.data.cassandra.repository.CassandraRepository;
import ru.hse.pensieve.database.cassandra.models.Thread;
import ru.hse.pensieve.database.cassandra.models.ThreadKey;

public interface ThreadRepository extends CassandraRepository<Thread, ThreadKey> {
}
