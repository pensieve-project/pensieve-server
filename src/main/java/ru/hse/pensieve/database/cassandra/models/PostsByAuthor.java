package ru.hse.pensieve.database.cassandra.models;

import org.springframework.data.cassandra.core.mapping.PrimaryKey;
import org.springframework.data.cassandra.core.mapping.Table;
import lombok.AllArgsConstructor;
import lombok.Setter;
import lombok.Getter;

import java.util.UUID;

@AllArgsConstructor
@Getter
@Setter
@Table("postsByAuthor")
public class PostsByAuthor {

    @PrimaryKey
    private PostsByAuthorKey key;

    private UUID postId;
    private String text;
}