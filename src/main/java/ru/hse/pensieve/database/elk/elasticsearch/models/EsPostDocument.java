package ru.hse.pensieve.database.elk.elasticsearch.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import org.springframework.data.elasticsearch.annotations.DateFormat;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;
import org.springframework.data.elasticsearch.annotations.GeoPointField;
import org.springframework.data.elasticsearch.core.geo.GeoPoint;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@Document(indexName = "posts_index")
public class EsPostDocument {
    @JsonProperty("postid")
    private UUID postId;

    @JsonProperty("authorid")
    private UUID authorId;

    @JsonProperty("themeid")
    @Field(type = FieldType.Keyword)
    private UUID themeId;

    @JsonProperty("timestamp")
    @Field(type = FieldType.Date, format = DateFormat.date_hour_minute_second)
    private Instant timeStamp;

    @Field(type = FieldType.Text)
    private String text;

    @JsonProperty("photoexists")
    @Field(type = FieldType.Boolean)
    private boolean photoExists;

    @GeoPointField
    private GeoPoint location;

    @JsonProperty("placename")
    @Field(type = FieldType.Text)
    private String placeName;

    @JsonProperty("coauthors")
    @Field(type = FieldType.Keyword)
    private Set<UUID> coAuthors;

    @JsonProperty("albumid")
    @Field(type = FieldType.Keyword)
    private UUID albumId;

    @JsonProperty("likescount")
    @Field(type = FieldType.Integer)
    private int likesCount;

    @JsonProperty("commentscount")
    @Field(type = FieldType.Integer)
    private int commentsCount;

    @JsonProperty("themetitle")
    @Field(type = FieldType.Text)
    private String themeTitle;

    @JsonProperty("authorusername")
    @Field(type = FieldType.Text)
    private String authorUsername;
}
