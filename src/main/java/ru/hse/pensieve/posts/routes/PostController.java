package ru.hse.pensieve.posts.routes;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.hse.pensieve.posts.models.*;
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

    @PostMapping("/like")
    public ResponseEntity<?> likePost(@RequestBody LikeRequest request) {
        postService.likePost(request);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/unlike")
    public ResponseEntity<?> unlikePost(@RequestBody LikeRequest request) {
        postService.unlikePost(request);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/liked")
    public ResponseEntity<Boolean> hasUserLikedPost(@RequestBody LikeRequest request) {
        return ResponseEntity.ok(postService.hasUserLikedPost(request));
    }

    @GetMapping("/likes-count")
    public ResponseEntity<Integer> getLikesCount(@RequestParam UUID postId) {
        return ResponseEntity.ok(postService.getLikesCount(postId));
    }

    @PostMapping("/comment")
    public ResponseEntity<CommentResponse> leaveComment(@RequestBody CommentRequest request) {
        return ResponseEntity.ok(postService.leaveComment(request));
    }

    @GetMapping("/comments")
    public ResponseEntity<List<CommentResponse>> getPostComments(@RequestParam UUID postId) {
        return ResponseEntity.ok(postService.getPostComments(postId));
    }

    @GetMapping("/comments-count")
    public ResponseEntity<Integer> getCommentsCount(@RequestParam UUID postId) {
        return ResponseEntity.ok(postService.getCommentsCount(postId));
    }
}
