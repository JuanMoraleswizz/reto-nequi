package com.nequi.franchises.shared;

import org.testcontainers.containers.PostgreSQLContainer;

public class PostgresTestContainer {

    private static final PostgreSQLContainer<?> CONTAINER =
            new PostgreSQLContainer<>("postgres:16-alpine")
                    .withDatabaseName("franchises_test")
                    .withUsername("postgres")
                    .withPassword("postgres");

    static {
        CONTAINER.start();
        System.setProperty("R2DBC_URL",
                "r2dbc:postgresql://" + CONTAINER.getHost() + ":" + CONTAINER.getFirstMappedPort() + "/franchises_test");
        System.setProperty("FLYWAY_URL", CONTAINER.getJdbcUrl());
        System.setProperty("POSTGRES_USER", CONTAINER.getUsername());
        System.setProperty("POSTGRES_PASSWORD", CONTAINER.getPassword());
    }

    public static PostgreSQLContainer<?> getInstance() {
        return CONTAINER;
    }
}
