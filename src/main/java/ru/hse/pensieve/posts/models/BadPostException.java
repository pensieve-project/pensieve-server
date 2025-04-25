package ru.hse.pensieve.posts.models;

public class BadPostException extends RuntimeException {
    public BadPostException(String message) {
        super(message);
    }
}