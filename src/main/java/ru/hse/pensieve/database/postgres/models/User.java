package ru.hse.pensieve.database.postgres.models;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.*;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
@Entity
@Table(name = "users", schema = "public")
@Data
@NoArgsConstructor
@Getter
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

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

    public User(String username, String email, String passwordHash, String salt) {
        this.username = username;
        this.email = email;
        this.passwordHash = passwordHash;
        this.salt = salt;
    }
}
