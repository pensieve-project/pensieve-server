package ru.hse.pensieve.database.cassandra.repositories;

import org.springframework.data.cassandra.repository.CassandraRepository;
import ru.hse.pensieve.database.cassandra.models.SubscribersByTarget;
import ru.hse.pensieve.database.cassandra.models.SubscribersByTargetKey;

import java.util.List;
import java.util.UUID;

public interface SubscribersByTargetRepository extends CassandraRepository<SubscribersByTarget, SubscribersByTargetKey> {
    List<SubscribersByTarget> findByKeyTargetId(UUID targetId);
}
