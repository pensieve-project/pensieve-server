package ru.hse.pensieve.database.cassandra.repositories;

import org.springframework.data.cassandra.repository.CassandraRepository;
import ru.hse.pensieve.database.cassandra.models.Album;
import ru.hse.pensieve.database.cassandra.models.AlbumKey;

import java.util.List;
import java.util.UUID;

public interface AlbumRepository extends CassandraRepository<Album, AlbumKey> {
    List<Album> findByKeyUserId(UUID userId);
}
