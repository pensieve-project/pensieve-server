package ru.hse.pensieve.database.cassandra.config;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import java.util.UUID;

@Configuration
@Getter
public class BucketConfig {

    @Getter
    private static int bucketCount;

    @Value("${cassandra.feed.buckets.count}")
    public void setBucketCount(int count) {
        bucketCount = count;
    }

    @Value("${cassandra.feed.max-partition-size-mb}")
    private int maxPartitionSizeMB;

    static public int getBucket(UUID postId) {
        return Math.abs(postId.hashCode()) % bucketCount;
    }

    public void adjustBucketCount(int currentPartitionSizeMB) {
        if (currentPartitionSizeMB > maxPartitionSizeMB) {
            bucketCount = Math.min(100, (int) Math.ceil(bucketCount * 1.5));
        }
    }
}