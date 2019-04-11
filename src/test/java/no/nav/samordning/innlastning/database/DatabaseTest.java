package no.nav.samordning.innlastning.database;

import no.nav.vault.jdbc.hikaricp.VaultError;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import com.zaxxer.hikari.HikariDataSource;

import static no.nav.samordning.innlastning.DatabaseTestUtils.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@Testcontainers
class DatabaseTest {

    private static final String VEDTAK_ID = "JKL678";
    private static final String IDENTIFIKATOR = "987654321";
    private static final String YTELSES_TYPE = "AAP";
    private static final String FOM = "2000-01-01";
    private static final String TOM = "2010-02-04";
    private static final String ROLE = "some_role";
    private static final String MOUNT_PATH = "mount/path";
    private static final String BAD_JDBC_URL = "badUrl";
    private static Database database;

    @Container
    private static PostgreSQLContainer postgresqlContainer = setUpPostgresContainer();

    @Container
    private static GenericContainer vaultContainer = setUpVaultContainer();

    @BeforeAll
    static void setup() throws Exception {
        runVaultContainerCommands(vaultContainer, postgresqlContainer.getContainerIpAddress());
        database = new Database(
                postgresqlContainer.getJdbcUrl(),
                DB_MOUNTPATH,
                DB_ROLE
        );
    }

    @Test
    void hendelse_inserted_to_db_as_json() throws Exception {

        Hendelse hendelse = new Hendelse();
        hendelse.setVedtakId(VEDTAK_ID);
        hendelse.setIdentifikator(IDENTIFIKATOR);
        hendelse.setYtelsesType(YTELSES_TYPE);
        hendelse.setFom(FOM);
        hendelse.setTom(TOM);

        database.insert(hendelse);

        String expectedHendelse = "{" +
                "\"fom\": \"" + FOM + "\", " +
                "\"tom\": \"" + TOM + "\", " +
                "\"vedtakId\": \"" + VEDTAK_ID + "\", " +
                "\"ytelsesType\": \"" + YTELSES_TYPE + "\", " +
                "\"identifikator\": \"" + IDENTIFIKATOR + "\"" +
                "}";

        HikariDataSource pgsqlDatasource = createPgsqlDatasource(postgresqlContainer);
        String actualHendelse = getFirstJsonHendelseFromDb(pgsqlDatasource);

        assertEquals(expectedHendelse, actualHendelse);
    }

    @Test
    void database_throws_VaultError_when_initialized_with_bad_environment_variables() {
        assertThrows(
                VaultError.class,
                () -> new Database(BAD_JDBC_URL, MOUNT_PATH, ROLE)
        );
    }
}
