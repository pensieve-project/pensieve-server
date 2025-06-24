package ru.hse.pensieve.database.elk.elasticsearch.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.elasticsearch.annotations.Document;

import java.time.Instant;
import java.util.UUID;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@Document(indexName = "themes_index")
public class EsThemeDocument {
    @JsonProperty("themeid")
    private UUID themeId;

    @JsonProperty("authorid")
    private UUID authorId;

    @JsonProperty("themetitle")
    private String title;

    @JsonProperty("timestamp")
    private Instant timeStamp;
}