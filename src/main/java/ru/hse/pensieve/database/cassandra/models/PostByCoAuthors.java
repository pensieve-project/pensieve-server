package ru.hse.pensieve.database.cassandra.models;

import org.springframework.data.cassandra.core.mapping.PrimaryKey;
import org.springframework.data.cassandra.core.mapping.Table;
import lombok.AllArgsConstructor;
import lombok.Setter;
import lombok.Getter;

import java.nio.ByteBuffer;

@AllArgsConstructor
@Getter
@Setter
@Table("posts_by_coauthors")
public class PostByCoAuthors {

    @PrimaryKey
    private PostByCoAuthorsKey key;

    private ByteBuffer photo;
    private String text;
    private Point location;
    private int likesCount;
    private int commentsCount;
}