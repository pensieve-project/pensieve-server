package ru.hse.pensieve.database.cassandra.models;

import lombok.NoArgsConstructor;
import org.springframework.data.cassandra.core.mapping.PrimaryKey;
import org.springframework.data.cassandra.core.mapping.Table;
import lombok.AllArgsConstructor;
import lombok.Setter;
import lombok.Getter;

import java.time.Instant;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Table("albums")
public class Album {

    @PrimaryKey
    private AlbumKey key;

    private Instant timeStamp;

}
