package ru.hse.pensieve.posts.models;

import ru.hse.pensieve.database.cassandra.models.Comment;

public class CommentMapper {
    public static CommentResponse fromComment(Comment comment) {
        return new CommentResponse(
                comment.getKey().getPostId(),
                comment.getKey().getCommentId(),
                comment.getAuthorId(),
                comment.getText()
        );
    }
}
