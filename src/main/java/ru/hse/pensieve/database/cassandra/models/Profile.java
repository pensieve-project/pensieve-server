package ru.hse.pensieve.database.cassandra.models;

import org.springframework.data.cassandra.core.mapping.PrimaryKey;
import org.springframework.data.cassandra.core.mapping.Table;
import lombok.AllArgsConstructor;
import lombok.Setter;
import lombok.Getter;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.UUID;

@AllArgsConstructor
@Getter
@Setter
@Table("profiles")
public class Profile {

    @PrimaryKey
    private UUID authorId;
    private ByteBuffer avatar;
    private String description;
    private ArrayList<UUID> likedThemesIds;
    private ArrayList<UUID> likedPostsIds;
    private Integer subscriptionsCount;
    private Integer subscribersCount;
    private Boolean isVip;
}
