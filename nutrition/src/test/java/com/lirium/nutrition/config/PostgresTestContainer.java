package com.lirium.nutrition.config;

import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.containers.PostgreSQLContainer;

/** Single PostgreSQL container shared by every integration test in the run. */
public interface PostgresTestContainer {

  @ServiceConnection
  PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:16-alpine");
}
