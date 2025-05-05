package ru.hse.pensieve.albums.service;

import ru.hse.pensieve.albums.models.*;
import ru.hse.pensieve.posts.models.PostResponse;

import java.util.List;
import java.util.Set;
import java.util.UUID;

public interface AlbumService {
    List<AlbumResponse> getUserAlbums(UUID userId);

    List<PostResponse> getAlbumPosts(Set<UUID> coAuthors);
}
