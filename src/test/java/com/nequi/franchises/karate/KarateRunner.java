package com.nequi.franchises.karate;

import com.intuit.karate.Results;
import com.intuit.karate.Runner;
import com.intuit.karate.core.FeatureResult;
import com.intuit.karate.Json;
import com.nequi.franchises.shared.PostgresTestContainer;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.containers.PostgreSQLContainer;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class KarateRunner {

    @LocalServerPort
    private int port;

    @BeforeAll
    static void startContainer() {
        PostgreSQLContainer<?> container = PostgresTestContainer.getInstance();
        System.setProperty("R2DBC_URL",
            "r2dbc:postgresql://" + container.getHost() + ":"
                + container.getMappedPort(5432) + "/" + container.getDatabaseName());
        System.setProperty("FLYWAY_URL",  container.getJdbcUrl());
        System.setProperty("POSTGRES_USER",     container.getUsername());
        System.setProperty("POSTGRES_PASSWORD", container.getPassword());
    }

    @Test
    void runAllKarateFeatures() {
        Results results = Runner
            .path("classpath:karate")
            .systemProperty("server.port", String.valueOf(port))
            .outputJunitXml(true)
            .parallel(3);

        writeCucumberJson(results);

        assertEquals(0, results.getFailCount(), results.getErrorMessages());
    }

    /**
     * Convierte los resultados de Karate a archivos JSON compatibles con
     * el formato Cucumber que espera el plugin Masterthought.
     */
    private void writeCucumberJson(Results results) {
        List<Map<String, Object>> cucumberReport = new ArrayList<>();
        results.getFeatureResults()
            .filter(fr -> !fr.isEmpty())
            .map(FeatureResult::toCucumberJson)
            .forEach(cucumberReport::add);

        File reportsDir = new File(results.getReportDir());
        File cucumberJsonFile = new File(reportsDir, "karate-cucumber.json");
        try (FileWriter writer = new FileWriter(cucumberJsonFile)) {
            writer.write(Json.of(cucumberReport).toString());
        } catch (IOException e) {
            throw new RuntimeException("No se pudo escribir el reporte Cucumber JSON", e);
        }
    }
}
