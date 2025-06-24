package ru.hse.pensieve.posts.service;

import ru.hse.pensieve.posts.models.*;

import java.util.List;
import java.util.Set;
import java.util.UUID;

public interface PostService {

    PostResponse savePost(PostRequest request) throws BadPostException;

    List<PostResponse> getAllPosts();

    List<PostResponse> getPostsByAuthor(UUID authorId);

    List<PostResponse> getPostsByTheme(UUID themeId);

    PostResponse getPostById(UUID postId);

    void likePost(LikeRequest request);

    void unlikePost(LikeRequest request);

    Boolean hasUserLikedPost(LikeRequest request);

    Integer getLikesCount(UUID postId);

    CommentResponse leaveComment(CommentRequest request);

    List<CommentResponse> getPostComments(UUID postId);

    Integer getCommentsCount(UUID postId);

    byte[] getPhoto(UUID postId);

    Set<UUID> getCoAuthors(UUID postId);
}
