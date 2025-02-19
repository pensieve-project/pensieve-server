package ru.hse.pensieve.posts;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import ru.hse.pensieve.database.models.Post;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/posts")
public class PostController {

    private final PostService postService;

    @Autowired
    public PostController(PostService postService) {
        this.postService = postService;
    }

    @PostMapping
    public Post createPost(@RequestBody PostRequest request) {
        return postService.savePost(request.getText(), request.getAuthorId(), request.getThreadId());
    }

    @GetMapping
    public Post getPost(@RequestParam UUID id) {
        return postService.getPostById(id);
    }

    @GetMapping("/by-author")
    public List<Post> getPostsByAuthor(@RequestParam UUID authorId) {
        return postService.getPostsByAuthor(authorId);
    }

    @GetMapping("/by-thread")
    public List<Post> getPostsByThread(@RequestParam UUID threadId) {
        return postService.getPostsByThread(threadId);
    }
}
