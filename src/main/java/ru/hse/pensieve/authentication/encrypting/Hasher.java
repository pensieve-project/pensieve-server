package ru.hse.pensieve.authentication.encrypting;

public interface Hasher {
    String hashWithSha256(String input);
}
