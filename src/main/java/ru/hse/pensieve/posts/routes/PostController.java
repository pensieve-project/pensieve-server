package ru.hse.pensieve.posts.routes;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.hse.pensieve.posts.models.PostRequest;
import ru.hse.pensieve.posts.models.PostResponse;
import ru.hse.pensieve.posts.service.PostService;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/posts")
public class PostController {

    @Autowired
    private PostService postService;

    @PostMapping
    public ResponseEntity<PostResponse> createPost(@ModelAttribute PostRequest request) {
        PostResponse response = new PostResponse();
        try {
            response = postService.savePost(request);
        } catch (IOException e) {
            System.out.println("Exception: " + e.getMessage());
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.status(201).body(response);
    }

    @GetMapping("/by-author")
    public ResponseEntity<List<PostResponse>> getPostsByAuthor(@RequestParam UUID authorId) {
        List<PostResponse> posts = postService.getPostsByAuthor(authorId);
        return ResponseEntity.ok(posts);
    }

    @GetMapping("/by-theme")
    public ResponseEntity<List<PostResponse>> getPostsByTheme(@RequestParam UUID themeId) {
        List<PostResponse> posts = postService.getPostsByTheme(themeId);
        return ResponseEntity.ok(posts);
    }
}
