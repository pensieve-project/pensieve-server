package ru.hse.pensieve.database.cassandra.config;

import com.datastax.oss.driver.api.core.CqlIdentifier;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.boot.autoconfigure.cassandra.CqlSessionBuilderCustomizer;

@Configuration
public class CassandraConfig {
    @Bean
    public CqlSessionBuilderCustomizer sessionBuilderCustomizer() {
        return builder -> builder
                .withLocalDatacenter("datacenter1")
                .withKeyspace(CqlIdentifier.fromCql("pensieve"));
    }
}
