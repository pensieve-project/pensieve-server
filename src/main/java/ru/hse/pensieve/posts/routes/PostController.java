package ru.hse.pensieve.posts.routes;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import ru.hse.pensieve.config.exceptions.ErrorResponse;
import ru.hse.pensieve.database.cassandra.models.Point;
import ru.hse.pensieve.posts.models.*;
import ru.hse.pensieve.posts.service.PostService;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/posts")
public class PostController {

    @Autowired
    private PostService postService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<PostResponse> createPost(
            @RequestPart(value = "text", required = false) String text,
            @RequestPart("photo") MultipartFile photo,
            @RequestPart("location") Point location,
            @RequestParam("authorId") UUID authorId,
            @RequestParam("themeId") UUID themeId,
            @RequestParam(value = "coAuthors", required = false) Set<UUID> coAuthors
    ) {
        PostResponse response = postService.savePost(new PostRequest(text, photo, location, authorId, themeId, coAuthors));
        return ResponseEntity.status(201).body(response);
    }

    @GetMapping
    public ResponseEntity<List<PostResponse>> getAllPosts() {
        List<PostResponse> posts = postService.getAllPosts();
        return ResponseEntity.ok(posts);
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

    @GetMapping("/by-id")
    public ResponseEntity<PostResponse> getPostById(@RequestParam UUID postId) {
        PostResponse post = postService.getPostById(postId);
        return ResponseEntity.ok(post);
    }

    @PostMapping("/like")
    public ResponseEntity<?> likePost(@RequestBody LikeRequest request) {
        postService.likePost(request);
        return ResponseEntity.status(201).build();
    }

    @DeleteMapping("/unlike")
    public ResponseEntity<?> unlikePost(@RequestParam UUID authorId, @RequestParam UUID postId) {
        postService.unlikePost(new LikeRequest(authorId, postId));
        return ResponseEntity.ok().build();
    }

    @GetMapping("/liked")
    public ResponseEntity<Boolean> hasUserLikedPost(@RequestParam UUID authorId, @RequestParam UUID postId) {
        return ResponseEntity.ok(postService.hasUserLikedPost(new LikeRequest(authorId, postId)));
    }

    @GetMapping("/likes-count")
    public ResponseEntity<Integer> getLikesCount(@RequestParam UUID postId) {
        return ResponseEntity.ok(postService.getLikesCount(postId));
    }

    @PostMapping("/comment")
    public ResponseEntity<CommentResponse> leaveComment(@RequestBody CommentRequest request) {
        return ResponseEntity.status(201).body(postService.leaveComment(request));
    }

    @GetMapping("/comments")
    public ResponseEntity<List<CommentResponse>> getPostComments(@RequestParam UUID postId) {
        return ResponseEntity.ok(postService.getPostComments(postId));
    }

    @GetMapping("/comments-count")
    public ResponseEntity<Integer> getCommentsCount(@RequestParam UUID postId) {
        return ResponseEntity.ok(postService.getCommentsCount(postId));
    }

    @ExceptionHandler(BadPostException.class)
    public ResponseEntity<ErrorResponse> handleBadPostException(Exception ex) {
        log.error("Post photo is null: ", ex);

        ErrorResponse error = new ErrorResponse(
                "Post photo is null: " + ex.getMessage(),
                HttpStatus.BAD_REQUEST.value()
        );
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(PostNotFoundException.class)
    public ResponseEntity<ErrorResponse> handlePostNotFoundException(Exception ex) {
        log.error("Post does not exist: ", ex);

        ErrorResponse error = new ErrorResponse(
                "Post does not exist: " + ex.getMessage(),
                HttpStatus.BAD_REQUEST.value()
        );
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }
}
