package ru.hse.pensieve.database.cassandra.repositories;

import org.springframework.data.cassandra.repository.CassandraRepository;
import org.springframework.data.cassandra.repository.Query;
import ru.hse.pensieve.database.cassandra.models.VipPost;
import ru.hse.pensieve.database.cassandra.models.VipPostKey;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface VipPostRepository extends CassandraRepository<VipPost, VipPostKey> {
    @Query("SELECT * FROM vip_posts WHERE authorId = ?0 AND timeStamp < ?1 LIMIT ?2")
    List<VipPost> findLatestByAuthor(UUID authorId, Instant before, int limit);
}
