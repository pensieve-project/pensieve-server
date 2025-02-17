package ru.hse.pensieve.database.repositories;

import java.util.UUID;

import org.springframework.data.repository.CrudRepository;
import ru.hse.pensieve.database.models.Post;

public interface PostRepository extends CrudRepository<Post, UUID> {
    Post findByText(String text);
}
