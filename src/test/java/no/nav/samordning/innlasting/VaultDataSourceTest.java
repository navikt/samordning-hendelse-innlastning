package no.nav.samordning.innlasting;

import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertThrows;

class VaultDataSourceTest {

    private static final String DB_URL_ENV_KEY = "DB_URL";
    private static final String DB_MOUNT_PATH_ENV_KEY = "DB_MOUNT_PATH";
    private static final String DB_ROLE_ENV_KEY = "DB_ROLE";

    @Test
    void throw_MissingApplicationConfig_when_database_url_is_missing_from_environment() {
        Map<String, String> testEnvironment = testEnvironmentWithouthProperty(DB_URL_ENV_KEY);
        assertThrows(
                MissingApplicationConfig.class,
                () -> new VaultDataSource(testEnvironment)
        );
    }

    @Test
    void throw_MissingApplicationConfig_when_mount_path_is_missing_from_environment() {
        Map<String, String> testEnvironment = testEnvironmentWithouthProperty(DB_MOUNT_PATH_ENV_KEY);
        assertThrows(
                MissingApplicationConfig.class,
                () -> new VaultDataSource(testEnvironment)
        );
    }

    @Test
    void throw_MissingApplicationConfig_when_database_role_is_missing_from_environment() {
        Map<String, String> testEnvironment = testEnvironmentWithouthProperty(DB_ROLE_ENV_KEY);
        assertThrows(
                MissingApplicationConfig.class,
                () -> new VaultDataSource(testEnvironment)
        );
    }

    private static Map<String, String> testEnvironmentWithouthProperty(String excludedProperty) {
        Map<String, String> testEnvironment = new HashMap<>();
        testEnvironment.put(DB_ROLE_ENV_KEY, "bogus");
        testEnvironment.put(DB_MOUNT_PATH_ENV_KEY, "bogus");
        testEnvironment.put(DB_URL_ENV_KEY, "bogus");
        testEnvironment.remove(excludedProperty);
        return testEnvironment;
    }
}
