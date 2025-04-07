package ru.hse.pensieve.database.elk.elasticsearch.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class EsThemeDocument {
    @JsonProperty("themeid")
    private UUID themeId;

    @JsonProperty("authorid")
    private UUID authorId;

    private String title;

    @JsonProperty("timestamp")
    private Instant timeStamp;
}