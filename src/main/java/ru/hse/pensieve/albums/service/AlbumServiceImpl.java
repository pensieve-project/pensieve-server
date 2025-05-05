package ru.hse.pensieve.albums.service;

import com.datastax.oss.driver.api.core.uuid.Uuids;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.hse.pensieve.database.cassandra.models.*;
import ru.hse.pensieve.database.cassandra.repositories.*;
import ru.hse.pensieve.database.redis.service.RedisService;
import ru.hse.pensieve.posts.kafka.PostEventProducer;
import ru.hse.pensieve.albums.models.*;
import ru.hse.pensieve.posts.models.PostMapper;
import ru.hse.pensieve.posts.models.PostResponse;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.time.Instant;
import java.util.*;

@Service
public class AlbumServiceImpl implements AlbumService {

    @Autowired
    private PostByCoAuthorsRepository postByCoAuthorsRepository;

    @Autowired
    private AlbumRepository albumRepository;

    public List<AlbumResponse> getUserAlbums(UUID userId) {
        return albumRepository.findByKeyUserId(userId).stream().map(AlbumMapper::fromAlbum).toList();
    }

    public List<PostResponse> getAlbumPosts(Set<UUID> coAuthors) {
        SortedSet<UUID> coAuthorsSorted = new TreeSet<>(coAuthors);
        return postByCoAuthorsRepository.findByKeyCoAuthors(coAuthorsSorted).stream().map(PostMapper::fromPostByCoAuthor).toList();
    }
}
