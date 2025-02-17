package ru.hse.pensieve.posts;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import ru.hse.pensieve.database.models.Post;

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
    public Post createPost(@RequestBody String text) {
        return postService.savePost(text);
    }

    @GetMapping
    public Post getPost(@RequestParam UUID id) {
        return postService.getPostById(id);
    }
}
