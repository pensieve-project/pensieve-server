
package ru.hse.pensieve.database.postgres.repositories;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

import org.springframework.data.repository.query.Param;
import ru.hse.pensieve.database.postgres.models.User;

public interface UserRepository extends CrudRepository<User, Integer> {
    boolean existsUserByUsername(String username);
    boolean existsUserByEmail(String email);
    @Query("SELECT u.salt FROM User u WHERE u.email = :email")
    Optional<String> findSaltByEmail(@Param("email") String email);
    Optional<User> findUserByEmailAndPasswordHash(String email, String passwordHash);
}
