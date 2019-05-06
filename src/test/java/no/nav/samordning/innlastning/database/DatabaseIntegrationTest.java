package no.nav.samordning.innlastning.database;

import com.zaxxer.hikari.HikariConfig;
import no.nav.samordning.schema.SamordningHendelse;
import no.nav.vault.jdbc.hikaricp.HikariCPVaultUtil;
import org.junit.jupiter.api.*;

import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import com.zaxxer.hikari.HikariDataSource;
import org.testcontainers.shaded.com.fasterxml.jackson.core.JsonProcessingException;
import org.testcontainers.shaded.com.fasterxml.jackson.databind.ObjectMapper;

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
    private static final ObjectMapper objectMapper = new ObjectMapper();

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

        String hendelseJson = getTestHendelseJson();
        database.insert(hendelseJson);

        String expectedHendelse = "{" +
                "\"identifikator\": \"" + IDENTIFIKATOR + "\", " +
                "\"ytelsesType\": \"" + YTELSES_TYPE + "\", " +
                "\"vedtakId\": \"" + VEDTAK_ID + "\", " +
                "\"fom\": \"" + FOM + "\", " +
                "\"tom\": \"" + TOM + "\"" +
                "}";
        String expectedHendelseJson = objectMapper.writeValueAsString(expectedHendelse);

        HikariDataSource pgsqlDatasource = createPgsqlDatasource(postgresqlContainer);
        String actualHendelse = getFirstJsonHendelseFromDb(pgsqlDatasource);

        assertEquals(expectedHendelseJson, actualHendelse);
    }

    @Test
    @Order(2)
    void insert_fails_when_database_is_down() {

        breakDatabaseConnection();

        assertThrows(
                FailedInsert.class,
                () -> database.insert(getTestHendelseJson())
        );
    }

    private void breakDatabaseConnection() {
        postgresqlContainer.stop();
        vaultContainer.stop();
    }

    private String getTestHendelseJson() throws Exception {
        SamordningHendelse samordningHendelse = new SamordningHendelse();
        samordningHendelse.setVedtakId(VEDTAK_ID);
        samordningHendelse.setIdentifikator(IDENTIFIKATOR);
        samordningHendelse.setYtelsesType(YTELSES_TYPE);
        samordningHendelse.setFom(FOM);
        samordningHendelse.setTom(TOM);
        return objectMapper.writeValueAsString(samordningHendelse.toString());
    }
}
