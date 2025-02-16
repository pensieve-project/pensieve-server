package ru.hse.pensieve;

import org.springframework.data.repository.CrudRepository;

public interface UserRepository extends CrudRepository<User, Integer> {
    boolean existsByUsername(String username);
}
