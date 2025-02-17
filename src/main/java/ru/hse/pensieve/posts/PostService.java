package ru.hse.pensieve.posts;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.hse.pensieve.database.models.Post;
import ru.hse.pensieve.database.repositories.PostRepository;

import java.util.UUID;

@Service
public class PostService {

    private final PostRepository postRepository;

    @Autowired
    public PostService(PostRepository postRepository) {
        this.postRepository = postRepository;
    }

    public Post savePost(String text) {
        Post post = new Post(UUID.randomUUID(), text);
        return postRepository.save(post);
    }

    public Post getPostById(UUID id) {
        return postRepository.findById(id).orElse(null);
    }
}
