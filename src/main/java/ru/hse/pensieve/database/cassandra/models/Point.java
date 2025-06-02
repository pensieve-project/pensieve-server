package ru.hse.pensieve.database.cassandra.models;

import org.springframework.data.cassandra.core.mapping.UserDefinedType;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.IOException;

@Data
@AllArgsConstructor
@NoArgsConstructor
@UserDefinedType("point_type")
public class Point {
    private double latitude;
    private double longitude;
    private String placeName;

    public static Point fromJson(String json) {
        try {
            return new ObjectMapper().readValue(json, Point.class);
        } catch (IOException e) {
            throw new IllegalArgumentException("Invalid location format", e);
        }
    }
}