package ru.hse.pensieve.database.cassandra.models;

import org.springframework.data.cassandra.core.mapping.PrimaryKey;
import org.springframework.data.cassandra.core.mapping.Table;
import lombok.AllArgsConstructor;
import lombok.Setter;
import lombok.Getter;

import java.nio.ByteBuffer;
import java.time.Instant;
import java.util.UUID;

@AllArgsConstructor
@Getter
@Setter
@Table("vip_posts")
public class VipPost {

    @PrimaryKey
    private VipPostKey key;

    private UUID themeId;
    private ByteBuffer photo;
    private String text;
    // geo
    // friends
    private int likesCount;
    private int commentsCount;
}