package ru.hse.pensieve.models;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ThemeImport {
    private String title;

    @JsonProperty("image_path")
    private String imagePath;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getImagePath() {
        return imagePath;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }
}
