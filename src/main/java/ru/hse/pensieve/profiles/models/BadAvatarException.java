package ru.hse.pensieve.profiles.models;

public class BadAvatarException extends RuntimeException {
    public BadAvatarException(String message) {
        super(message);
    }
}