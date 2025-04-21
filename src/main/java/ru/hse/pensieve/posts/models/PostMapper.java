package ru.hse.pensieve.posts.models;

import ru.hse.pensieve.database.cassandra.models.Post;
import ru.hse.pensieve.database.cassandra.models.PostByAuthor;
import ru.hse.pensieve.database.cassandra.models.PostById;

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
}
