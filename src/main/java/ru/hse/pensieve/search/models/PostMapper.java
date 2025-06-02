package ru.hse.pensieve.search.models;

import ru.hse.pensieve.database.cassandra.models.Point;
import ru.hse.pensieve.database.elk.elasticsearch.models.EsPostDocument;
import ru.hse.pensieve.posts.models.PostResponse;

public class PostMapper {
    public static PostResponse fromEsPost(EsPostDocument esPost) {
        Point location = new Point(
            esPost.getLocation().getLat(),
            esPost.getLocation().getLon(),
            esPost.getPlaceName()
        );
        
        return new PostResponse(
            esPost.getThemeId(),
            esPost.getAuthorId(),
            esPost.getPostId(),
            location,
            esPost.getCoAuthors(),
            esPost.getAlbumId(),
            null,
            esPost.getText(),
            esPost.getTimeStamp(),
            esPost.getLikesCount(),
            esPost.getCommentsCount()
        );
    }
}
