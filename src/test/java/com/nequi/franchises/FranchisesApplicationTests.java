package com.nequi.franchises;

import com.nequi.franchises.shared.PostgresTestContainer;
import org.junit.jupiter.api.BeforeAll;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.containers.PostgreSQLContainer;

@SpringBootTest
@ActiveProfiles("test")
class FranchisesApplicationTests {

    @BeforeAll
    static void startContainer() {
        PostgreSQLContainer<?> container = PostgresTestContainer.getInstance();
        System.setProperty("R2DBC_URL", "r2dbc:postgresql://" + container.getHost() + ":" + container.getMappedPort(5432) + "/" + container.getDatabaseName());
        System.setProperty("FLYWAY_URL", container.getJdbcUrl());
        System.setProperty("POSTGRES_USER", container.getUsername());
        System.setProperty("POSTGRES_PASSWORD", container.getPassword());
    }

    void contextLoads() {}
}
