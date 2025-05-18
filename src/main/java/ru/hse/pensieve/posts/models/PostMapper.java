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
                post.getCoAuthors(),
                post.getAlbumId(),
                post.getPhoto() != null ? post.getPhoto().array() : null,
                post.getText(),
                post.getKey().getTimeStamp(),
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
                post.getCoAuthors(),
                post.getAlbumId(),
                post.getPhoto() != null ? post.getPhoto().array() : null,
                post.getText(),
                post.getKey().getTimeStamp(),
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
                post.getCoAuthors(),
                post.getAlbumId(),
                post.getPhoto() != null ? post.getPhoto().array() : null,
                post.getText(),
                post.getKey().getTimeStamp(),
                post.getLikesCount(),
                post.getCommentsCount()
        );
    }

    public static PostResponse fromPostByAlbum(PostByAlbum post) {
        return new PostResponse(
                post.getKey().getThemeId(),
                post.getKey().getAuthorId(),
                post.getKey().getPostId(),
                post.getLocation() != null ? post.getLocation() : null,
                post.getCoAuthors(),
                post.getKey().getAlbumId(),
                post.getPhoto() != null ? post.getPhoto().array() : null,
                post.getText(),
                post.getKey().getTimeStamp(),
                post.getLikesCount(),
                post.getCommentsCount()
        );
    }

    public static Post postFromUserFeed(UserFeed userFeed) {
        return new Post(
                new PostKey(
                        userFeed.getThemeId(),
                        userFeed.getKey().getTimeStamp(),
                        userFeed.getAuthorId(),
                        userFeed.getKey().getPostId()
                ),
                userFeed.getPhoto(),
                userFeed.getText(),
                userFeed.getLocation(),
                userFeed.getCoAuthors(),
                userFeed.getAlbumId(),
                userFeed.getLikesCount(),
                userFeed.getCommentsCount()
        );
    }

    public static UserFeed feedFromPost(Post post, UUID userId, Integer bucket) {
        return new UserFeed(
                new UserFeedKey(
                        userId,
                        bucket,
                        post.getKey().getTimeStamp(),
                        post.getKey().getPostId()
                ),
                post.getKey().getThemeId(),
                post.getKey().getAuthorId(),
                post.getPhoto(),
                post.getText(),
                post.getLocation(),
                post.getCoAuthors(),
                post.getAlbumId(),
                post.getLikesCount(),
                post.getCommentsCount()
        );
    }

    public static UserFeed feedFromPostByAuthor(PostByAuthor post, UUID userId, Integer bucket) {
        return new UserFeed(
                new UserFeedKey(
                        userId,
                        bucket,
                        post.getKey().getTimeStamp(),
                        post.getKey().getPostId()
                ),
                post.getKey().getThemeId(),
                post.getKey().getAuthorId(),
                post.getPhoto(),
                post.getText(),
                post.getLocation(),
                post.getCoAuthors(),
                post.getAlbumId(),
                post.getLikesCount(),
                post.getCommentsCount()
        );
    }

    public static Post postFromPostById(PostById post) {
        return new Post(
                new PostKey(
                        post.getKey().getThemeId(),
                        post.getKey().getTimeStamp(),
                        post.getKey().getAuthorId(),
                        post.getKey().getPostId()
                ),
                post.getPhoto(),
                post.getText(),
                post.getLocation(),
                post.getCoAuthors(),
                post.getAlbumId(),
                post.getLikesCount(),
                post.getCommentsCount()
        );
    }

    public static Post postFromPostByAuthor(PostByAuthor post) {
        return new Post(
                new PostKey(
                        post.getKey().getThemeId(),
                        post.getKey().getTimeStamp(),
                        post.getKey().getAuthorId(),
                        post.getKey().getPostId()
                ),
                post.getPhoto(),
                post.getText(),
                post.getLocation(),
                post.getCoAuthors(),
                post.getAlbumId(),
                post.getLikesCount(),
                post.getCommentsCount()
        );
    }
}
