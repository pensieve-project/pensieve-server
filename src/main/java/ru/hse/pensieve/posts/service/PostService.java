package ru.hse.pensieve.posts.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.hse.pensieve.database.cassandra.models.Post;
import ru.hse.pensieve.database.cassandra.models.PostKey;
import ru.hse.pensieve.database.cassandra.repositories.PostRepository;
import ru.hse.pensieve.database.cassandra.repositories.PostByAuthorRepository;
import ru.hse.pensieve.posts.models.PostRequest;
import ru.hse.pensieve.posts.models.PostResponse;
import ru.hse.pensieve.posts.models.PostMapper;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
public class PostService {

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private PostByAuthorRepository postByAuthorRepository;

    public PostResponse savePost(PostRequest request) throws IOException {
        PostKey postKey = new PostKey(request.getThemeId(), request.getAuthorId(), UUID.randomUUID());
        byte[] photoBytes = (request.getPhoto() != null && !request.getPhoto().isEmpty()) ? request.getPhoto().getBytes() : null;
        if (photoBytes == null) {
            throw new IOException();
        }
        Post post = new Post(postKey, ByteBuffer.wrap(photoBytes), request.getText(), Instant.now(), 0);
        Post newPost = postRepository.save(post);
        return PostMapper.fromPost(newPost);
    }

    public List<PostResponse> getPostsByAuthor(UUID authorId) {
        return postByAuthorRepository.findByKeyAuthorId(authorId).stream().map(PostMapper::fromPostByAuthor).toList();
    }

    public List<PostResponse> getPostsByTheme(UUID themeId) {
        return postRepository.findByKeyThemeId(themeId).stream().map(PostMapper::fromPost).toList();
    }
}
