package ru.hse.pensieve.posts.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.hse.pensieve.database.cassandra.models.Post;
import ru.hse.pensieve.database.cassandra.models.PostKey;
import ru.hse.pensieve.database.cassandra.repositories.PostRepository;
import ru.hse.pensieve.database.cassandra.repositories.PostByAuthorRepository;
import ru.hse.pensieve.posts.models.PostRequest;

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

    public Post savePost(PostRequest request) {
        PostKey postKey = new PostKey(request.getThreadId(), request.getAuthorId());
        Post post = new Post(postKey, UUID.randomUUID(), request.getText(), Instant.now(), 0);
        return postRepository.save(post);
    }

    public List<Post> getPostsByAuthor(UUID authorId) {
        return postByAuthorRepository.findByKeyAuthorId(authorId).stream().map(Post::new).toList();
    }

    public List<Post> getPostsByThread(UUID threadId) {
        return postRepository.findByKeyThreadId(threadId);
    }
}
