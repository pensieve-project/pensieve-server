package ru.hse.pensieve;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
@Entity
@Table(name = "users", schema = "public")
@Data
@NoArgsConstructor
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @Column(name = "username")
    private String username;

    @Column(name = "passwordHash")
    @JsonIgnore
    private String passwordHash;

    public User(String username, String passwordHash) {
        this.username = username;
        this.passwordHash = passwordHash;
    }

    public Integer getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }
}
