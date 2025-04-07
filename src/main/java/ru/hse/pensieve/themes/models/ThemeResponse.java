package ru.hse.pensieve.themes.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ThemeResponse {
    @JsonProperty("themeid")
    private UUID themeId;

    @JsonProperty("authorid")
    private UUID authorId;

    private String title;

    @JsonProperty("timestamp")
    private Instant timeStamp;
}
