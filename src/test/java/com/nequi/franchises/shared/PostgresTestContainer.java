package com.nequi.franchises.shared;

import org.testcontainers.containers.PostgreSQLContainer;

public class PostgresTestContainer {

    private static final PostgreSQLContainer<?> CONTAINER;

    static {
        CONTAINER = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("franchises_test")
            .withUsername("test")
            .withPassword("test");
        CONTAINER.start();
    }

    public static PostgreSQLContainer<?> getInstance() {
        return CONTAINER;
    }
}
