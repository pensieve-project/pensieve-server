package ru.hse.pensieve.database.redis.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import ru.hse.pensieve.database.cassandra.models.Post;

import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class RedisService {

    @Autowired
    private RedisTemplate<String, Post> redisTemplate;

    private static final String KEY_PATTERN = "vip-post:%s:%s";

    public void cacheVipPost(Post post) {
        String key = String.format(KEY_PATTERN,
                post.getKey().getAuthorId(),
                post.getKey().getPostId()
        );
        redisTemplate.opsForValue().set(key, post);
    }

    public Post getVipPost(UUID authorId, UUID postId) {
        return redisTemplate.opsForValue().get(
                String.format(KEY_PATTERN, authorId, postId)
        );
    }

    public void deleteVipPost(UUID authorId, UUID postId) {
        redisTemplate.delete(
                String.format(KEY_PATTERN, authorId, postId)
        );
    }

    public void deleteAllPostsByAuthor(UUID authorId) {
        Set<String> keys = redisTemplate.keys(
                String.format("vip-post:%s:*", authorId)
        );

        if (!keys.isEmpty()) {
            redisTemplate.delete(keys);
        }
    }

    public List<Post> getLatestVipPosts(UUID authorId, Instant before, int limit) {
        String listKey = "vip-feed::" + authorId;
        List<Post> posts = redisTemplate.opsForList().range(listKey, 0, limit + 10);
        if (posts == null) {
            return List.of();
        }
        return posts.stream()
                .filter(p -> p.getKey().getTimeStamp().isBefore(before))
                .sorted(Comparator.<Post, Instant>comparing(post -> post.getKey().getTimeStamp()).reversed())
                .limit(limit)
                .collect(Collectors.toList());
    }
}
