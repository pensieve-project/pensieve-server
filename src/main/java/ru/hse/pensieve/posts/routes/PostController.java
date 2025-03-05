package ru.hse.pensieve.posts.routes;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import ru.hse.pensieve.database.cassandra.models.Post;
import ru.hse.pensieve.posts.models.PostRequest;
import ru.hse.pensieve.posts.models.PostResponse;
import ru.hse.pensieve.posts.service.PostService;

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
    public PostResponse createPost(@RequestBody PostRequest request) {
        return postService.savePost(request);
    }

    @GetMapping("/by-author")
    public List<PostResponse> getPostsByAuthor(@RequestParam UUID authorId) {
        return postService.getPostsByAuthor(authorId);
    }

    @GetMapping("/by-theme")
    public List<PostResponse> getPostsByTheme(@RequestParam UUID themeId) {
        return postService.getPostsByTheme(themeId);
    }
}
