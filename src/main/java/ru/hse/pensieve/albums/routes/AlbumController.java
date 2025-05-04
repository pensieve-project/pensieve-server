package ru.hse.pensieve.albums.routes;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.hse.pensieve.albums.service.AlbumService;
import ru.hse.pensieve.albums.models.*;
import ru.hse.pensieve.posts.models.PostResponse;

import java.util.List;
import java.util.Set;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/albums")
public class AlbumController {

    @Autowired
    private AlbumService albumService;

    @GetMapping
    public ResponseEntity<List<AlbumResponse>> getUserAlbums(@RequestParam UUID userId) {
        return ResponseEntity.ok(albumService.getUserAlbums(userId));
    }

    @GetMapping("/posts")
    public ResponseEntity<List<PostResponse>> getAlbumPosts(@RequestParam Set<UUID> coAuthors) {
        return ResponseEntity.ok(albumService.getAlbumPosts(coAuthors));
    }

}
