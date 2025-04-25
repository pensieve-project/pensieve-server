package ru.hse.pensieve.database.redis.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import ru.hse.pensieve.database.cassandra.models.Post;

import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class RedisService {

    @Autowired
    private RedisTemplate<String, Post> redisTemplate;

    private final String PREFIX = "vip-post::";

    public void cacheVipPost(Post post) {
        String key = PREFIX + post.getKey().getPostId().toString();
        redisTemplate.opsForValue().set(key, post);
    }

    public Post getVipPost(UUID postId) {
        String key = PREFIX + postId.toString();
        return redisTemplate.opsForValue().get(key);
    }

    public void deleteVipPost(UUID postId) {
        redisTemplate.delete(PREFIX + postId.toString());
    }

    public List<Post> getLatestVipPosts(UUID authorId, Instant before, int limit) {
        String listKey = "vip-feed::" + authorId;
        List<Post> posts = redisTemplate.opsForList().range(listKey, 0, limit + 10);
        if (posts == null) {
            return List.of();
        }
        return posts.stream()
                .filter(p -> p.getTimeStamp().isBefore(before))
                .sorted(Comparator.comparing(Post::getTimeStamp).reversed())
                .limit(limit)
                .collect(Collectors.toList());
    }
}
