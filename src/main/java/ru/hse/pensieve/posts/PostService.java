package ru.hse.pensieve.posts;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.hse.pensieve.database.cassandra.models.Post;
import ru.hse.pensieve.database.cassandra.repositories.PostRepository;

import java.util.List;
import java.util.UUID;

@Service
public class PostService {

    private final PostRepository postRepository;

    @Autowired
    public PostService(PostRepository postRepository) {
        this.postRepository = postRepository;
    }

    public Post savePost(String text, UUID author_id, UUID thread_id) {
        Post post = new Post(UUID.randomUUID(), text, author_id, thread_id);
        return postRepository.save(post);
    }

    public Post getPostById(UUID id) {
        return postRepository.findById(id).orElse(null);
    }

    public List<Post> getPostsByAuthor(UUID authorId) {
        return postRepository.findByAuthorId(authorId);
    }

    public List<Post> getPostsByThread(UUID threadId) {
        return postRepository.findByThreadId(threadId);
    }
}
