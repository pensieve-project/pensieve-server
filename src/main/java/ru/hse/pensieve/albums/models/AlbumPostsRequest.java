package ru.hse.pensieve.albums.models;

import lombok.*;
import java.util.Set;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
public class AlbumPostsRequest {
    private Set<UUID> coAuthors;
}
