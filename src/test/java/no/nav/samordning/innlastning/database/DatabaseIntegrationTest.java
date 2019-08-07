package no.nav.samordning.innlastning.database;

import no.nav.samordning.innlastning.DatabaseTestUtils;
import no.nav.samordning.schema.SamordningHendelse;
import org.junit.jupiter.api.*;

import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import com.zaxxer.hikari.HikariDataSource;
import org.testcontainers.shaded.com.fasterxml.jackson.databind.ObjectMapper;

import static no.nav.samordning.innlastning.DatabaseTestUtils.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@Testcontainers
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class DatabaseIntegrationTest {

    private static final String TPNR = "1234";
    private static final String VEDTAK_ID = "JKL678";
    private static final String IDENTIFIKATOR = "987654321";
    private static final String YTELSES_TYPE = "AAP";
    private static final String FOM = "2000-01-01";
    private static final String TOM = "2010-02-04";

    private static Database database;
    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Container
    private static PostgreSQLContainer postgresqlContainer = setUpPostgresContainer();

    @BeforeAll
    static void setUp() {
        HikariDataSource datasource = DatabaseTestUtils.createPgsqlDatasource(postgresqlContainer);
        database = new Database(datasource);
    }

    @Test
    @Order(1)
    void hendelse_inserted_to_db_as_json() throws Exception {

        String hendelseJson = getTestHendelseJson();
        database.insert(hendelseJson, TPNR);

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
        String actualTpnr = getFirstTpnrFromDb(pgsqlDatasource);

        assertEquals(expectedHendelseJson, actualHendelse);
        assertEquals(TPNR, actualTpnr);
    }

    @Test
    @Order(2)
    void insert_fails_when_database_is_down() {

        breakDatabaseConnection();

        assertThrows(
                FailedInsert.class,
                () -> database.insert(getTestHendelseJson(), TPNR)
        );
    }

    private void breakDatabaseConnection() {
        postgresqlContainer.stop();
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
