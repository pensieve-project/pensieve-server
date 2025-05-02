package ru.hse.pensieve.database.cassandra.models;

import org.springframework.data.cassandra.core.mapping.UserDefinedType;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import java.io.IOException;

@Data
@UserDefinedType("point_type")
public class Point {
    private double latitude;
    private double longitude;

    public static Point fromJson(String json) {
        try {
            return new ObjectMapper().readValue(json, Point.class);
        } catch (IOException e) {
            throw new IllegalArgumentException("Invalid location format", e);
        }
    }
}