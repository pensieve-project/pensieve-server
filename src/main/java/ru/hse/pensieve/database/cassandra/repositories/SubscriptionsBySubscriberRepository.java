package ru.hse.pensieve.database.cassandra.repositories;

import org.springframework.data.cassandra.repository.CassandraRepository;
import ru.hse.pensieve.database.cassandra.models.SubscriptionsBySubscriberKey;
import ru.hse.pensieve.database.cassandra.models.SubscriptionsBySubscriber;

import java.util.List;
import java.util.UUID;

public interface SubscriptionsBySubscriberRepository extends CassandraRepository<SubscriptionsBySubscriber, SubscriptionsBySubscriberKey> {
    List<SubscriptionsBySubscriber> findByKeySubscriberId(UUID subscriberId);
}
