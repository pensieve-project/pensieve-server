package ru.hse.pensieve.posts.models;

import lombok.*;
import org.springframework.web.multipart.MultipartFile;
import ru.hse.pensieve.database.cassandra.models.Point;

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

    public Point getLocationPoint() {
        return Point.fromJson(location);
    }
}
