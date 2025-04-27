package ru.hse.pensieve.posts.models;

import ru.hse.pensieve.database.cassandra.models.*;

import java.util.UUID;

public class PostMapper {
    public static PostResponse fromPost(Post post) {
        return new PostResponse(
                post.getKey().getThemeId(),
                post.getKey().getAuthorId(),
                post.getKey().getPostId(),
                post.getLocation() != null ? post.getLocation() : null,
                post.getPhoto() != null ? post.getPhoto().array() : null,
                post.getText(),
                post.getTimeStamp(),
                post.getLikesCount(),
                post.getCommentsCount()
        );
    }

    public static PostResponse fromPostByAuthor(PostByAuthor post) {
        return new PostResponse(
                post.getKey().getThemeId(),
                post.getKey().getAuthorId(),
                post.getKey().getPostId(),
                post.getLocation() != null ? post.getLocation() : null,
                post.getPhoto() != null ? post.getPhoto().array() : null,
                post.getText(),
                post.getTimeStamp(),
                post.getLikesCount(),
                post.getCommentsCount()
        );
    }

    public static PostResponse fromPostById(PostById post) {
        return new PostResponse(
                post.getKey().getThemeId(),
                post.getKey().getAuthorId(),
                post.getKey().getPostId(),
                post.getLocation() != null ? post.getLocation() : null,
                post.getPhoto() != null ? post.getPhoto().array() : null,
                post.getText(),
                post.getTimeStamp(),
                post.getLikesCount(),
                post.getCommentsCount()
        );
    }

    public static VipPost vipFromPost(Post post) {
        return new VipPost(
                new VipPostKey(
                        post.getKey().getAuthorId(),
                        post.getTimeStamp(),
                        post.getKey().getPostId()
                ),
                post.getKey().getThemeId(),
                post.getPhoto(),
                post.getText(),
                post.getLocation(),
                post.getLikesCount(),
                post.getCommentsCount()
        );
    }

    public static Post postFromUserFeed(UserFeed userFeed) {
        return new Post(
                new PostKey(
                        userFeed.getThemeId(),
                        userFeed.getAuthorId(),
                        userFeed.getKey().getPostId()
                ),
                userFeed.getPhoto(),
                userFeed.getText(),
                userFeed.getKey().getTimeStamp(),
                userFeed.getLocation(),
                userFeed.getLikesCount(),
                userFeed.getCommentsCount()
        );
    }

    public static Post postFromVip(VipPost vipPost) {
        return new Post(
                new PostKey(
                        vipPost.getThemeId(),
                        vipPost.getKey().getAuthorId(),
                        vipPost.getKey().getPostId()
                ),
                vipPost.getPhoto(),
                vipPost.getText(),
                vipPost.getKey().getTimeStamp(),
                vipPost.getLocation(),
                vipPost.getLikesCount(),
                vipPost.getCommentsCount()
        );
    }

    public static UserFeed feedFromPost(Post post, UUID userId, Integer bucket) {
        return new UserFeed(
                new UserFeedKey(
                        userId,
                        bucket,
                        post.getTimeStamp(),
                        post.getKey().getPostId()
                ),
                post.getKey().getThemeId(),
                post.getKey().getAuthorId(),
                post.getPhoto(),
                post.getText(),
                post.getLocation(),
                post.getLikesCount(),
                post.getCommentsCount()
        );
    }

    public static UserFeed feedFromPostByAuthor(PostByAuthor post, UUID userId, Integer bucket) {
        return new UserFeed(
                new UserFeedKey(
                        userId,
                        bucket,
                        post.getTimeStamp(),
                        post.getKey().getPostId()
                ),
                post.getKey().getThemeId(),
                post.getKey().getAuthorId(),
                post.getPhoto(),
                post.getText(),
                post.getLocation(),
                post.getLikesCount(),
                post.getCommentsCount()
        );
    }

    public static Post postFromPostById(PostById post) {
        return new Post(
                new PostKey(
                        post.getKey().getThemeId(),
                        post.getKey().getAuthorId(),
                        post.getKey().getPostId()
                ),
                post.getPhoto(),
                post.getText(),
                post.getTimeStamp(),
                post.getLocation(),
                post.getLikesCount(),
                post.getCommentsCount()
        );
    }
}
