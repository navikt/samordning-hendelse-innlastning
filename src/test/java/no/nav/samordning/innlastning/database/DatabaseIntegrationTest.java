package no.nav.samordning.innlastning.database;

import com.zaxxer.hikari.HikariConfig;
import no.nav.vault.jdbc.hikaricp.HikariCPVaultUtil;
import org.junit.jupiter.api.*;

import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import com.zaxxer.hikari.HikariDataSource;

import static no.nav.samordning.innlastning.DatabaseTestUtils.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@Testcontainers
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class DatabaseIntegrationTest {

    private static final String VEDTAK_ID = "JKL678";
    private static final String IDENTIFIKATOR = "987654321";
    private static final String YTELSES_TYPE = "AAP";
    private static final String FOM = "2000-01-01";
    private static final String TOM = "2010-02-04";

    private static Database database;

    @Container
    private static PostgreSQLContainer postgresqlContainer = setUpPostgresContainer();

    @Container
    private static GenericContainer vaultContainer = setUpVaultContainer();

    @BeforeAll
    static void setUp() throws Exception {
        runVaultContainerCommands(vaultContainer, postgresqlContainer.getContainerIpAddress());
        HikariConfig datasourceConfig = DatasourceConfig.getDatasourceConfig(postgresqlContainer.getJdbcUrl());
        HikariDataSource datasourceWithVaultIntegration = HikariCPVaultUtil.createHikariDataSourceWithVaultIntegration(datasourceConfig, DB_MOUNT_PATH, DB_ROLE);
        database = new Database(datasourceWithVaultIntegration);
    }

    @Test
    @Order(1)
    void hendelse_inserted_to_db_as_json() throws Exception {

        Hendelse hendelse = getTestHendelse();
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
    @Order(2)
    void insert_fails_when_database_is_down() {

        breakDatabaseConnection();

        assertThrows(
                FailedInsert.class,
                () -> database.insert(getTestHendelse())
        );
    }

    private void breakDatabaseConnection() {
        postgresqlContainer.stop();
        vaultContainer.stop();
    }

    private Hendelse getTestHendelse() {
        Hendelse hendelse = new Hendelse();
        hendelse.setVedtakId(VEDTAK_ID);
        hendelse.setIdentifikator(IDENTIFIKATOR);
        hendelse.setYtelsesType(YTELSES_TYPE);
        hendelse.setFom(FOM);
        hendelse.setTom(TOM);
        return hendelse;
    }
}
