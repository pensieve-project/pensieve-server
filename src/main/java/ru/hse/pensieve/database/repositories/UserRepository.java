package ru.hse.pensieve.database.repositories;

import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

import ru.hse.pensieve.database.models.User;

public interface UserRepository extends CrudRepository<User, Integer> {
    boolean existsUserByUsername(String username);
    Optional<String> findSaltByUsername(String username);
    Optional<User> findUserByUsernameAndPasswordHash(String username, String passwordHash);
}
