package ru.hse.pensieve.posts.models;

import lombok.*;
import org.springframework.web.multipart.MultipartFile;
import ru.hse.pensieve.database.cassandra.models.Point;

import java.util.ArrayList;
import java.util.Set;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
public class PostRequest {
    private String text;
    private MultipartFile photo;
    private String location;
    private UUID authorId;
    private UUID themeId;
    private Set<UUID> coAuthors;

    public Point getLocationPoint() {
        return Point.fromJson(location);
    }
}
