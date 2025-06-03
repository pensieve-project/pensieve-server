package ru.hse.pensieve.database.redis.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import ru.hse.pensieve.database.cassandra.models.Post;

@Configuration
public class RedisConfig {

    @Bean
    public RedisTemplate<String, Post> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, Post> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);
        template.setKeySerializer(new StringRedisSerializer());
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        Jackson2JsonRedisSerializer<Post> serializer = new Jackson2JsonRedisSerializer<>(objectMapper, Post.class);
        template.setValueSerializer(serializer);
        template.afterPropertiesSet();
        return template;
    }
}
