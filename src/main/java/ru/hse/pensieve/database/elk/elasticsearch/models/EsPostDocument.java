package ru.hse.pensieve.database.elk.elasticsearch.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.core.geo.GeoPoint;

import java.time.Instant;
import java.util.UUID;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@Document(indexName = "posts_index")
public class EsPostDocument {

    @JsonProperty("themeid")
    private UUID themeId;

    @JsonProperty("authorid")
    private UUID authorId;

    @JsonProperty("postid")
    private UUID postId;

    private String text;

    @JsonProperty("timestamp")
    private Instant timeStamp;

    private GeoPoint location;

    @JsonProperty("albumid")
    private UUID albumId;

    @JsonProperty("likescount")
    private int likesCount;

    @JsonProperty("commentscount")
    private int commentsCount;

    @JsonProperty("themetitle")
    private String themeTitle;

    @JsonProperty("authorusername")
    private String authorUsername;

    @JsonProperty("placename")
    private String placeName;
}