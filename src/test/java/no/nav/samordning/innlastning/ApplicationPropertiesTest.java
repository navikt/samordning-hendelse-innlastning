package no.nav.samordning.innlastning;

import no.nav.samordning.innlastning.ApplicationProperties;
import no.nav.samordning.innlastning.MissingApplicationConfig;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class ApplicationPropertiesTest {

    private static final String JDBC_URL = "jdbc_url";
    private static final String JDBC_URL_KEY = "DB_URL";

    private static Map<String, String> environment = new HashMap<>();

    @Test
    public void getDbPropertyFromEnvironment_returns() {
        environment.put(JDBC_URL_KEY, JDBC_URL);
        String jdbcUrl = ApplicationProperties.getFromEnvironment(environment, JDBC_URL_KEY);

        assertEquals(JDBC_URL, jdbcUrl);
    }

    @Test
    public void getDbPropertyFromEnvironment_throws_MissingDatabaseConfig_when_property_has_no_value_in_environment() {
        environment.put(JDBC_URL_KEY, null);
        assertThrows(MissingApplicationConfig.class,
                () -> ApplicationProperties.getFromEnvironment(environment, JDBC_URL_KEY)
        );
    }

    @Test
    public void getDbPropertyFromEnvironment_throws_MissingDatabaseConfig_when_property_is_not_in_environment() {
        assertThrows(MissingApplicationConfig.class,
                () -> ApplicationProperties.getFromEnvironment(environment, null)
        );
    }

}
