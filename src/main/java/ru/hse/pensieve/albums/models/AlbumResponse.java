package ru.hse.pensieve.albums.models;

import lombok.*;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class AlbumResponse {
    private UUID userId;
    private Set<UUID> coAuthors;
    private Instant timeStamp;
}
