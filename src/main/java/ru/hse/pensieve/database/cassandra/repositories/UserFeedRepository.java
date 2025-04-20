package ru.hse.pensieve.database.cassandra.repositories;

import org.springframework.data.cassandra.repository.CassandraRepository;
import org.springframework.data.cassandra.repository.Query;
import ru.hse.pensieve.database.cassandra.config.FeedTaskExecutor;
import ru.hse.pensieve.database.cassandra.config.BucketConfig;
import ru.hse.pensieve.database.cassandra.models.UserFeed;
import ru.hse.pensieve.database.cassandra.models.UserFeedKey;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public interface UserFeedRepository extends CassandraRepository<UserFeed, UserFeedKey> {
    @Query("SELECT * FROM user_feed WHERE userId = ?0 AND bucket = ?1 AND timeStamp < ?2 LIMIT ?3")
    List<UserFeed> findFeedPage(UUID userId, int bucket, Instant lastSeenTime, int limit);

    default List<UserFeed> findUserFeed(UUID userId, int limit, Instant lastSeenTime) {
        int buckets = BucketConfig.getBucketCount();
        int perBucketLimit = (int) Math.ceil((double) limit / buckets) + 1;

        List<CompletableFuture<List<UserFeed>>> futures = new ArrayList<>(buckets);

        for (int bucket = 0; bucket < buckets; bucket++) {
            int finalBucket = bucket;
            futures.set(bucket, CompletableFuture.supplyAsync(() -> findFeedPage(userId, finalBucket, lastSeenTime, perBucketLimit), FeedTaskExecutor.getExecutor()));
        }

        return futures.stream()
                .map(CompletableFuture::join)
                .flatMap(List::stream)
                .sorted(Comparator.comparing(feed -> feed.getKey().getTimeStamp(), Comparator.reverseOrder()))
                .limit(limit)
                .collect(Collectors.toList());
    }
}
