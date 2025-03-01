package ru.hse.pensieve.database.postgres.models;

import com.fasterxml.jackson.annotation.JsonIgnore;

import org.hibernate.annotations.UuidGenerator;

import java.util.UUID;

import jakarta.persistence.*;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "users", schema = "public")
@Data
@NoArgsConstructor
@Getter
@Setter
public class User {
    @Id
    @GeneratedValue
    @UuidGenerator
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "username")
    private String username;

    @Column(name = "email")
    private String email;

    @Column(name = "passwordHash")
    @JsonIgnore
    private String passwordHash;

    @Column(name = "salt")
    @JsonIgnore
    private String salt;

    @Column(name = "refreshToken")
    private String refreshToken;

    public User(String username, String email, String passwordHash, String salt) {
        this.username = username;
        this.email = email;
        this.passwordHash = passwordHash;
        this.salt = salt;
    }
}
