package org.edu.pet.cloud_file_storage.controller;

import com.redis.testcontainers.RedisContainer;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.postgresql.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

public abstract class AbstractAuthIntegrationTest {

    @Container
    @ServiceConnection
    protected static final PostgreSQLContainer POSTGRESQL = new PostgreSQLContainer("postgres:16");

    @Container
    @ServiceConnection
    protected static final RedisContainer REDIS = new RedisContainer(DockerImageName.parse("redis:8.0.3"));
}