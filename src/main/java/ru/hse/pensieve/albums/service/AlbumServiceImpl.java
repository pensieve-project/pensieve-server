package ru.hse.pensieve.albums.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.hse.pensieve.database.cassandra.repositories.*;
import ru.hse.pensieve.albums.models.*;
import ru.hse.pensieve.posts.models.PostMapper;
import ru.hse.pensieve.posts.models.PostResponse;

import java.util.*;

@Service
public class AlbumServiceImpl implements AlbumService {

    @Autowired
    private PostByAlbumRepository postByCoAuthorsRepository;

    @Autowired
    private AlbumRepository albumRepository;

    public List<AlbumResponse> getUserAlbums(UUID userId) {
        return albumRepository.findByKeyUserId(userId).stream().map(AlbumMapper::fromAlbum).toList();
    }

    public List<PostResponse> getAlbumPosts(UUID albumId) {
        return postByCoAuthorsRepository.findByKeyAlbumId(albumId).stream().map(PostMapper::fromPostByAlbum).toList();
    }
}
