package ru.hse.pensieve.database.cassandra.repositories;

import org.springframework.data.cassandra.repository.CassandraRepository;
import ru.hse.pensieve.database.cassandra.models.Profile;

import java.util.UUID;

public interface ProfileRepository extends CassandraRepository<Profile, UUID> {
}
