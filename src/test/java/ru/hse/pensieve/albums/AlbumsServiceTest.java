package ru.hse.pensieve.albums;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.hse.pensieve.albums.service.AlbumServiceImpl;
import ru.hse.pensieve.database.cassandra.models.Album;
import ru.hse.pensieve.database.cassandra.models.AlbumKey;
import ru.hse.pensieve.database.cassandra.models.PostByAlbum;
import ru.hse.pensieve.database.cassandra.models.PostByAlbumKey;
import ru.hse.pensieve.database.cassandra.repositories.AlbumRepository;
import ru.hse.pensieve.database.cassandra.repositories.PostByAlbumRepository;
import ru.hse.pensieve.albums.models.AlbumResponse;
import ru.hse.pensieve.posts.models.PostResponse;

import java.nio.ByteBuffer;
import java.time.Instant;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AlbumsServiceTest {

    @Mock
    private AlbumRepository albumRepository;

    @Mock
    private PostByAlbumRepository postByAlbumRepository;

    @InjectMocks
    private AlbumServiceImpl albumService;

    private final UUID userId = UUID.randomUUID();
    private final UUID albumId = UUID.randomUUID();
    private final Set<UUID> coAuthors = Set.of(UUID.randomUUID(), UUID.randomUUID(), userId);

    @Test
    void getUserAlbums_ShouldReturnAlbumsWithCoAuthors() {
        AlbumKey key1 = new AlbumKey(userId, Set.of(userId, UUID.randomUUID()));
        AlbumKey key2 = new AlbumKey(userId, coAuthors);

        List<Album> albums = List.of(
                new Album(key1, UUID.randomUUID(), ByteBuffer.wrap(new byte[0])),
                new Album(key2, UUID.randomUUID(), ByteBuffer.wrap(new byte[0]))
        );

        when(albumRepository.findByKeyUserId(userId)).thenReturn(albums);

        List<AlbumResponse> result = albumService.getUserAlbums(userId);

        assertEquals(2, result.size());
        assertEquals(coAuthors, result.get(1).getCoAuthors());
        verify(albumRepository).findByKeyUserId(userId);
    }

    @Test
    void getUserAlbums_ShouldHandleEmptyCoAuthors() {
        when(albumRepository.findByKeyUserId(userId)).thenReturn(List.of());

        assertTrue(albumService.getUserAlbums(userId).isEmpty());
    }

    @Test
    void getAlbumPosts_ShouldReturnPostsWithCorrectAlbumId() {
        PostByAlbum post1 = createPost("Content 1");
        PostByAlbum post2 = createPost("Content 2");

        when(postByAlbumRepository.findByKeyAlbumId(albumId)).thenReturn(List.of(post1, post2));

        List<PostResponse> result = albumService.getAlbumPosts(albumId);

        assertEquals(2, result.size());
        assertTrue(result.stream().allMatch(p -> p.getAlbumId().equals(albumId)));
    }

    @Test
    void getAlbumPosts_ShouldHandleBinaryContent() {
        byte[] avatarBytes = {0x12, 0x34, 0x56};
        PostByAlbum post = createPost("Content");
        post.setPhoto(ByteBuffer.wrap(avatarBytes));

        when(postByAlbumRepository.findByKeyAlbumId(albumId)).thenReturn(List.of(post));

        PostResponse result = albumService.getAlbumPosts(albumId).get(0);

        assertArrayEquals(avatarBytes, result.getPhoto());
    }

    @Test
    void getUserAlbums_ShouldMapAvatarCorrectly() {
        byte[] avatarData = {0x01, 0x02, 0x03};
        Album album = new Album(
                new AlbumKey(userId, coAuthors),
                albumId,
                ByteBuffer.wrap(avatarData)
        );

        when(albumRepository.findByKeyUserId(userId)).thenReturn(List.of(album));

        AlbumResponse response = albumService.getUserAlbums(userId).get(0);

        assertArrayEquals(avatarData, response.getAvatar().array());
    }

    @Test
    void getUserAlbums_ShouldHandleNullAvatar() {
        Album album = new Album(new AlbumKey(userId, coAuthors), albumId, null);
        when(albumRepository.findByKeyUserId(userId)).thenReturn(List.of(album));

        AlbumResponse response = albumService.getUserAlbums(userId).get(0);

        assertNull(response.getAvatar());
    }

    private PostByAlbum createPost(String content) {
        return new PostByAlbum(
                new PostByAlbumKey(albumId, Instant.now(), userId, UUID.randomUUID(), UUID.randomUUID()),
                ByteBuffer.wrap(new byte[0]),
                content,
                null,
                null,
                0,
                0
        );
    }
}