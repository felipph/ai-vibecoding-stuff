package com.example.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

/**
 * Testcontainers configuration for integration tests.
 *
 * This configuration uses:
 * - PostgreSQL 16 Alpine for lightweight database testing
 * - @ServiceConnection for automatic DataSource configuration
 * - Static container for reuse across test classes
 *
 * Spring Boot 4 / Testcontainers 2.x pattern with @TestConfiguration.
 */
@TestConfiguration(proxyBeanMethods = false)
@Testcontainers
public class TestcontainersConfiguration {

    /**
     * PostgreSQL container configured with:
     * - PostgreSQL 16 Alpine (lightweight image)
     * - Reusable container (static field)
     * - Auto-configuration via @ServiceConnection
     */
    @Container
    static PostgreSQLContainer<?> postgres =
        new PostgreSQLContainer<>(DockerImageName.parse("postgres:16-alpine"))
            .withReuse(true);  // Reuse container across tests for performance

    /**
     * Exposes the container as a bean with @ServiceConnection.
     * Spring Boot automatically configures the DataSource from this.
     */
    @Bean
    @ServiceConnection
    PostgreSQLContainer<?> postgresContainer() {
        return postgres;
    }
}
