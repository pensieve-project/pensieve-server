package ru.hse.pensieve.posts.service;

import com.datastax.oss.driver.api.core.uuid.Uuids;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.hse.pensieve.database.cassandra.models.*;
import ru.hse.pensieve.database.cassandra.repositories.*;
import ru.hse.pensieve.posts.models.*;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class PostService {

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private PostByAuthorRepository postByAuthorRepository;

    @Autowired
    private PostByIdRepository postByIdRepository;

    @Autowired
    private ProfileRepository profileRepository;

    @Autowired
    private CommentRepository commentRepository;

    public PostResponse savePost(PostRequest request) throws IOException {
        PostKey postKey = new PostKey(request.getThemeId(), request.getAuthorId(), UUID.randomUUID());
        byte[] photoBytes = (request.getPhoto() != null && !request.getPhoto().isEmpty()) ? request.getPhoto().getBytes() : null;
        if (photoBytes == null) {
            throw new IOException();
        }
        Post post = new Post(postKey, ByteBuffer.wrap(photoBytes), request.getText(), Instant.now(), 0, 0);
        Post newPost = postRepository.save(post);
        return PostMapper.fromPost(newPost);
    }

    public List<PostResponse> getPostsByAuthor(UUID authorId) {
        return postByAuthorRepository.findByKeyAuthorId(authorId).stream().map(PostMapper::fromPostByAuthor).toList();
    }

    public List<PostResponse> getPostsByTheme(UUID themeId) {
        return postRepository.findByKeyThemeId(themeId).stream().map(PostMapper::fromPost).toList();
    }

    public void likePost(LikeRequest request) {
        if (hasUserLikedPost(request)) {
            return;
        }
        List<PostById> posts = postByIdRepository.findByKeyPostId(request.getPostId());
        if (posts.isEmpty()) {
            return;
        }

        PostById post = posts.getFirst();
        post.setLikesCount(post.getLikesCount() + 1);
        postRepository.save(new Post(
                new PostKey(
                        post.getKey().getThemeId(),
                        post.getKey().getAuthorId(),
                        post.getKey().getPostId()
                ),
                post.getPhoto(),
                post.getText(),
                post.getTimeStamp(),
                post.getLikesCount(),
                post.getCommentsCount()
        ));

        Profile profile = profileRepository.findByAuthorId(request.getAuthorId());
        ArrayList<UUID> likes = profile.getLikedPostsIds();
        if (likes == null) {
            likes = new ArrayList<>();
        }
        likes.add(request.getPostId());
        profile.setLikedPostsIds(likes);
        profileRepository.save(profile);
    }

    public void unlikePost(LikeRequest request) {
        if (!hasUserLikedPost(request)) {
            return;
        }
        List<PostById> posts = postByIdRepository.findByKeyPostId(request.getPostId());
        if (posts.isEmpty()) {
            return;
        }

        PostById post = posts.getFirst();
        post.setLikesCount(post.getLikesCount() - 1);
        postRepository.save(new Post(
                new PostKey(
                        post.getKey().getThemeId(),
                        post.getKey().getAuthorId(),
                        post.getKey().getPostId()
                ),
                post.getPhoto(),
                post.getText(),
                post.getTimeStamp(),
                post.getLikesCount(),
                post.getCommentsCount()
        ));

        Profile profile = profileRepository.findByAuthorId(request.getAuthorId());
        ArrayList<UUID> likes = profile.getLikedPostsIds();
        if (likes == null) {
            return;
        }
        likes.remove(request.getPostId());
        profile.setLikedPostsIds(likes);
        profileRepository.save(profile);
    }

    public Boolean hasUserLikedPost(LikeRequest request) {
        return profileRepository.hasLikedPost(request.getAuthorId(), request.getPostId());
    }

    public Integer getLikesCount(UUID postId) {
        PostById post = postByIdRepository.findByKeyPostId(postId).getFirst();
        return post.getLikesCount();
    }

    public CommentResponse leaveComment(CommentRequest request) {
        CommentKey key = new CommentKey(request.getPostId(), Uuids.timeBased());
        Comment comment = new Comment(key, request.getAuthorId(), request.getText());
        Comment newComment = commentRepository.save(comment);
        List<PostById> posts = postByIdRepository.findByKeyPostId(newComment.getKey().getPostId());
        if (posts.isEmpty()) {
            throw new RuntimeException(); // handle!!
        }

        PostById post = posts.getFirst();
        post.setCommentsCount(post.getCommentsCount() + 1);
        postRepository.save(new Post(
                new PostKey(
                        post.getKey().getThemeId(),
                        post.getKey().getAuthorId(),
                        post.getKey().getPostId()
                ),
                post.getPhoto(),
                post.getText(),
                post.getTimeStamp(),
                post.getLikesCount(),
                post.getCommentsCount()
        ));
        return CommentMapper.fromComment(newComment);
    }

    public List<CommentResponse> getPostComments(UUID postId) {
        return commentRepository.findByKeyPostId(postId).stream().map(CommentMapper::fromComment).toList();
    }

    public Integer getCommentsCount(UUID postId) {
        PostById post = postByIdRepository.findByKeyPostId(postId).getFirst();
        return post.getCommentsCount();
    }
}
