package ru.hse.pensieve.database.cassandra.repositories;

import org.springframework.data.cassandra.repository.CassandraRepository;
import ru.hse.pensieve.database.cassandra.models.Theme;

import java.util.List;
import java.util.UUID;

public interface ThemeRepository extends CassandraRepository<Theme, UUID> {
    List<Theme> findByThemeId(UUID themeId);
}
