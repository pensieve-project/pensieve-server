package ru.hse.pensieve.database.cassandra.repositories;

import org.springframework.data.cassandra.repository.CassandraRepository;
import ru.hse.pensieve.database.cassandra.models.PostByAlbum;
import ru.hse.pensieve.database.cassandra.models.PostByAlbumKey;

import java.util.List;
import java.util.UUID;

public interface PostByAlbumRepository extends CassandraRepository<PostByAlbum, PostByAlbumKey> {
    List<PostByAlbum> findByKeyAlbumId(UUID albumId);
}
