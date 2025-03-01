package ru.hse.pensieve.database.postgres.repositories;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;
import ru.hse.pensieve.database.postgres.models.User;

public interface UserRepository extends CrudRepository<User, UUID> {
    boolean existsUserByUsername(String username);
    boolean existsUserByEmail(String email);
    boolean existsByRefreshToken(String refreshToken);
    @Query("SELECT u.salt FROM User u WHERE u.email = :email")
    Optional<String> findSaltByEmail(@Param("email") String email);
    Optional<User> findUserByEmailAndPasswordHash(String email, String passwordHash);
    @Modifying
    @Transactional
    @Query("UPDATE User u SET u.refreshToken = :refreshToken WHERE u.refreshToken = :oldRefreshToken")
    void updateRefreshToken(@Param("oldRefreshToken") String oldRefreshToken, @Param("refreshToken") String refreshToken);
    @Modifying
    @Transactional
    @Query("UPDATE User u SET u.refreshToken = :refreshToken WHERE u.id = :userId")
    void updateRefreshTokenById(@Param("userId") UUID userId, @Param("refreshToken") String refreshToken);
}
