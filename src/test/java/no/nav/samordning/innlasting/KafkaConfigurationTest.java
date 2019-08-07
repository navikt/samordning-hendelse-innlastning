package no.nav.samordning.innlasting;

import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static no.nav.samordning.innlasting.KafkaConfiguration.Properties.*;
import static org.junit.jupiter.api.Assertions.assertThrows;

class KafkaConfigurationTest {

    @Test
    void MissingApplicationConfig_is_thrown_when_bootstrap_servers_are_missing() {
        Map<String, String> testEnvironment = testEnvironmentWithouthProperty(BOOTSTRAP_SERVERS);
        assertThrows(
                MissingApplicationConfig.class,
                () -> new KafkaConfiguration(testEnvironment)
        );
    }

    @Test
    void MissingApplicationConfig_is_thrown_when_kafka_username_is_missing() {
        Map<String, String> testEnvironment = testEnvironmentWithouthProperty(USERNAME);
        assertThrows(
                MissingApplicationConfig.class,
                () -> new KafkaConfiguration(testEnvironment)
        );
    }

    @Test
    void MissingApplicationConfig_is_thrown_when_kafka_password_is_missing() {
        Map<String, String> testEnvironment = testEnvironmentWithouthProperty(PASSWORD);
        assertThrows(
                MissingApplicationConfig.class,
                () -> new KafkaConfiguration(testEnvironment)
        );
    }

    private static Map<String, String> testEnvironmentWithouthProperty(String excludedProperty) {
        Map<String, String> testEnvironment = new HashMap<>();
        testEnvironment.put(BOOTSTRAP_SERVERS, "bogus");
        testEnvironment.put(USERNAME, "bogus");
        testEnvironment.put(PASSWORD, "bogus");
        testEnvironment.remove(excludedProperty);
        return testEnvironment;
    }
}
