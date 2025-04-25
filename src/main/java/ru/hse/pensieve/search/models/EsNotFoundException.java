package ru.hse.pensieve.search.models;

public class EsNotFoundException extends RuntimeException {
    public EsNotFoundException(String message) {
        super(message);
    }
}