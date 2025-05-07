package ru.hse.pensieve.albums.models;

import ru.hse.pensieve.database.cassandra.models.*;

public class AlbumMapper {
    public static AlbumResponse fromAlbum(Album post) {
        return new AlbumResponse(
                post.getKey().getUserId(),
                post.getKey().getCoAuthors(),
                post.getAlbumId(),
                post.getAvatar()
        );
    }
}
