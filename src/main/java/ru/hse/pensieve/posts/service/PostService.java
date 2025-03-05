package ru.hse.pensieve.posts.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.hse.pensieve.database.cassandra.models.Post;
import ru.hse.pensieve.database.cassandra.models.PostKey;
import ru.hse.pensieve.database.cassandra.repositories.PostRepository;
import ru.hse.pensieve.database.cassandra.repositories.PostByAuthorRepository;
import ru.hse.pensieve.posts.models.PostRequest;
import ru.hse.pensieve.posts.models.PostResponse;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
public class PostService {

    private final PostRepository postRepository;
    private final PostByAuthorRepository postByAuthorRepository;

    @Autowired
    public PostService(PostRepository postRepository, PostByAuthorRepository postsByAuthorRepository) {
        this.postRepository = postRepository;
        this.postByAuthorRepository = postsByAuthorRepository;
    }

    public PostResponse savePost(PostRequest request) {
        PostKey postKey = new PostKey(request.getThemeId(), request.getAuthorId(), UUID.randomUUID());
        Post post = new Post(postKey, request.getText(), Instant.now(), 0);
        return new PostResponse(postRepository.save(post));
    }

    public List<PostResponse> getPostsByAuthor(UUID authorId) {
        return postByAuthorRepository.findByKeyAuthorId(authorId).stream().map(PostResponse::new).toList();
    }

    public List<PostResponse> getPostsByTheme(UUID themeId) {
        return postRepository.findByKeyThemeId(themeId).stream().map(PostResponse::new).toList();
    }
}
