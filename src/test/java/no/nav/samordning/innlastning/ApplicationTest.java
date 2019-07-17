package no.nav.samordning.innlastning;

import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertThrows;

class ApplicationTest {

    private static final String KAFKA_BOOTSTRAP_SERVERS_ENV_KEY = "KAFKA_BOOTSTRAP_SERVERS";
    private static final String KAFKA_USERNAME_ENV_KEY = "KAFKA_USERNAME";
    private static final String KAFKA_PASSWORD_ENV_KEY = "KAFKA_PASSWORD";
    private static final String DB_URL_ENV_KEY = "DB_URL";
    private static final String DB_MOUNT_PATH_ENV_KEY = "DB_MOUNT_PATH";
    private static final String DB_ROLE_ENV_KEY = "DB_ROLE";

    @Test
    void Application_throws_MissingApplicationConfig_when_kafka_config_is_missing_from_environment() {
        Map<String, String> testEnvironment = Collections.emptyMap();
        assertThrows(
                MissingApplicationConfig.class,
                () -> new Application(testEnvironment)
        );
    }

    @Test
    void Application_throws_MissingApplicationConfig_when_database_config_is_missing_from_environment() {
        Map<String, String> testEnvironment = new HashMap<>();
        testEnvironment.put(KAFKA_BOOTSTRAP_SERVERS_ENV_KEY, "bogus");
        testEnvironment.put(KAFKA_USERNAME_ENV_KEY, "bogus");
        testEnvironment.put(KAFKA_PASSWORD_ENV_KEY, "bogus");
        assertThrows(
                MissingApplicationConfig.class,
                () -> new Application(testEnvironment)
        );
    }

    @Test
    void Application_throws_MissingVaultToken_when_vault_token_is_missing() {
        Map<String, String> testEnvironment = new HashMap<>();
        testEnvironment.put(KAFKA_BOOTSTRAP_SERVERS_ENV_KEY, "bogus");
        testEnvironment.put(KAFKA_USERNAME_ENV_KEY, "bogus");
        testEnvironment.put(KAFKA_PASSWORD_ENV_KEY, "bogus");
        testEnvironment.put(DB_URL_ENV_KEY, "bogus");
        testEnvironment.put(DB_MOUNT_PATH_ENV_KEY, "bogus");
        testEnvironment.put(DB_ROLE_ENV_KEY, "bogus");
        assertThrows(
                MissingVaultToken.class,
                () -> new Application(testEnvironment)
        );
    }
}
