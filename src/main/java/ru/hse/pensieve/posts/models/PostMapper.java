package ru.hse.pensieve.posts.models;

import ru.hse.pensieve.database.cassandra.models.Post;
import ru.hse.pensieve.database.cassandra.models.PostByAuthor;

public class PostMapper {
    public static PostResponse fromPost(Post post) {
        return new PostResponse(
                post.getKey().getThemeId(),
                post.getKey().getAuthorId(),
                post.getKey().getPostId(),
                post.getPhoto() != null ? post.getPhoto().array() : null,
                post.getText(),
                post.getTimeStamp(),
                post.getLikesCount()
        );
    }

    public static PostResponse fromPostByAuthor(PostByAuthor post) {
        return new PostResponse(
                post.getKey().getThemeId(),
                post.getKey().getAuthorId(),
                post.getKey().getPostId(),
                post.getPhoto() != null ? post.getPhoto().array() : null,
                post.getText(),
                post.getTimeStamp(),
                post.getLikesCount()
        );
    }
}
