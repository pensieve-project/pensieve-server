package ru.hse.pensieve.database.postgres.repositories;

import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

import ru.hse.pensieve.database.postgres.models.User;

public interface UserRepository extends CrudRepository<User, Integer> {
    boolean existsUserByUsername(String username);
    boolean existsUserByEmail(String email);
    Optional<String> findSaltByEmail(String email);
    Optional<User> findUserByEmailAndPasswordHash(String email, String passwordHash);
}
