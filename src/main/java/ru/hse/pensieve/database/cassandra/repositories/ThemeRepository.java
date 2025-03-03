package ru.hse.pensieve.database.cassandra.repositories;

import org.springframework.data.cassandra.repository.CassandraRepository;
import ru.hse.pensieve.database.cassandra.models.Theme;
import ru.hse.pensieve.database.cassandra.models.ThemeKey;

public interface ThemeRepository extends CassandraRepository<Theme, ThemeKey> {
}
