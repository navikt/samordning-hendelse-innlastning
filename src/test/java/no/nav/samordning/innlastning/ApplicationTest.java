package no.nav.samordning.innlastning;

import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertThrows;

public class ApplicationTest {

    @Test
    public void Application_throws_MissingApplicationConfig_when_database_config_is_missing_from_environment() {
        Map<String, String> testEnvironment = new HashMap<>();
        testEnvironment.put("KAFKA_BOOTSTRAP_SERVERS", "brokersUrl");
        testEnvironment.put("SCHEMA_REGISTRY_URL", "schema.registry.url");
        testEnvironment.put("KAFKA_USERNAME", "kafka_username");
        testEnvironment.put("KAFKA_PASSWORD", "opensourcedPassword");
        testEnvironment.put("KAFKA_SASL_MECHANISM", "PLAIN");

        assertThrows(
                MissingApplicationConfig.class,
                () -> new Application(testEnvironment)
        );
    }
}
