package ru.hse.pensieve.database.redis.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import ru.hse.pensieve.database.cassandra.models.Post;
import ru.hse.pensieve.database.cassandra.models.PostById;
import ru.hse.pensieve.database.cassandra.repositories.PostByIdRepository;
import ru.hse.pensieve.posts.models.PostMapper;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PostRankingService {
    private final RedisTemplate<String, Post> redisTemplate;
    private final RedisTemplate<String, String> stringRedisTemplate;
    private final PostByIdRepository postByIdRepository;

    private static final String POST_KEY = "post:%s";
    private static final String LIKES_KEY = "post-likes:%s";
    private static final String POPULAR_KEY = "popular-24h";

    public void cachePost(Post post, long ttlHours) {
        redisTemplate.opsForValue().set(
                String.format(POST_KEY, post.getKey().getPostId()),
                post,
                ttlHours,
                TimeUnit.HOURS
        );
    }

    public Post getCachedPost(UUID postId) {
        return redisTemplate.opsForValue().get(String.format(POST_KEY, postId));
    }

    public void addLike(UUID postId, UUID userId) {
        long now = System.currentTimeMillis();
        stringRedisTemplate.opsForZSet().add(
                String.format(LIKES_KEY, postId),
                userId.toString(),
                now
        );
        updatePostRanking(postId);
    }

    public void removeLike(UUID postId, UUID userId) {
        stringRedisTemplate.opsForZSet().remove(
                String.format(LIKES_KEY, postId),
                userId.toString()
        );
        updatePostRanking(postId);
    }

    private void updatePostRanking(UUID postId) {
        Post post = getCachedPost(postId);
        if (post == null) {
            List<PostById> posts = postByIdRepository.findByKeyPostId(postId);
            post = PostMapper.postFromPostById(posts.getFirst());
        }
        cachePost(post, 24L);
        long twentyFourHoursAgo = System.currentTimeMillis() - 86_400_000;
        stringRedisTemplate.opsForZSet().removeRangeByScore(
                String.format(LIKES_KEY, postId),
                0,
                twentyFourHoursAgo
        );
        Long likesCount = stringRedisTemplate.opsForZSet().zCard(
                String.format(LIKES_KEY, postId)
        );
        if (likesCount != null && likesCount > 0) {
            stringRedisTemplate.opsForZSet().add(
                    POPULAR_KEY,
                    postId.toString(),
                    likesCount
            );
        } else {
            stringRedisTemplate.opsForZSet().remove(
                    POPULAR_KEY,
                    postId.toString()
            );
        }
    }

    public List<Post> getTopPosts(int limit) {
        Set<String> postIds = stringRedisTemplate.opsForZSet()
                .reverseRange(POPULAR_KEY, 0, limit - 1);
        if (postIds == null) {
            System.out.println();
            return List.of();
        }

        return redisTemplate.executePipelined((RedisCallback<Post>) connection -> {
                    postIds.forEach(id ->
                            connection.stringCommands().get(
                                    String.format(POST_KEY, id).getBytes()
                            )
                    );
                    return null;
                }).stream()
                .filter(Objects::nonNull)
                .map(obj -> (Post) obj)
                .collect(Collectors.toList());
    }

    @Scheduled(fixedRate = 60000)
    public void trimPopularPosts() {
        Long size = stringRedisTemplate.opsForZSet().zCard(POPULAR_KEY);
        if (size != null && size > 300) {
            stringRedisTemplate.opsForZSet().removeRange(
                    POPULAR_KEY,
                    0,
                    size - 300 - 1
            );
        }
    }
}
