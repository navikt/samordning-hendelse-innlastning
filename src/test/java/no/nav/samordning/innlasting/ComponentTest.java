package no.nav.samordning.innlasting;

import no.nav.samordning.schema.SamordningHendelse;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.shaded.com.fasterxml.jackson.databind.ObjectMapper;

import javax.sql.DataSource;

import static java.lang.Thread.sleep;
import static no.nav.samordning.innlasting.Application.ApplicationDataSource;
import static no.nav.samordning.innlasting.DatabaseTestUtils.*;
import static no.nav.samordning.innlasting.KafkaTestEnvironment.kafkaConfiguration;
import static no.nav.samordning.innlasting.KafkaTestEnvironment.populate_hendelse_topic;
import static no.nav.samordning.innlasting.NaisEndpointTest.*;
import static org.junit.jupiter.api.Assertions.assertEquals;


@Testcontainers
class ComponentTest {

    private static final String TPNR = "1234";
    private static final String IDENTIFIKATOR = "12345678901";
    private static final String YTELSESTYPE = "AP";
    private static final String VEDTAK_ID = "ABC123";
    private static final String FOM = "01-01-2020";
    private static final String TOM = "01-01-2010";

    @Container
    private static PostgreSQLContainer postgresqlContainer = setUpPostgresContainer();

    private static Application app;

    @BeforeAll
    static void setUp() {
        System.setProperty("zookeeper.jmx.log4j.disable", Boolean.TRUE.toString());
        var dataSourceWithoutVaultIntegration = new DataSourceWithoutVaultIntegration();
        KafkaTestEnvironment.setup();
        app = new Application(dataSourceWithoutVaultIntegration, kafkaConfiguration);
        app.run();
    }

    @AfterAll
    static void tearDown() {
        app.shutdown();
    }

    @Test
    void innlasting_reads_hendelser_from_kafka_and_persists_hendelse_to_db() throws Exception {

        var samordningHendelse = new SamordningHendelse(IDENTIFIKATOR, YTELSESTYPE, VEDTAK_ID, FOM, TOM);

        var expectedHendelse = new ObjectMapper().writeValueAsString("{" +
                "\"identifikator\": \"" + IDENTIFIKATOR + "\", " +
                "\"ytelsesType\": \"" + YTELSESTYPE + "\", " +
                "\"vedtakId\": \"" + VEDTAK_ID + "\", " +
                "\"fom\": \"" + FOM + "\", " +
                "\"tom\": \"" + TOM + "\"" +
                "}");

        populate_hendelse_topic(TPNR, samordningHendelse);

        //Application needs to process records before the tests resume
        sleep(5 * 1000);

        nais_platform_prerequisites_runs_OK();

        var postgresqlDatasource = createPgsqlDatasource(postgresqlContainer);
        var actualHendelse = getFirstJsonHendelseFromDb(postgresqlDatasource);
        var actualTpnr = getFirstTpnrFromDb(postgresqlDatasource);

        assertEquals(expectedHendelse, actualHendelse);
        assertEquals(TPNR, actualTpnr);

    }

    private void nais_platform_prerequisites_runs_OK() throws Exception {
        isAlive_endpoint_returns_200_OK_when_application_runs();
        isReady_endpoint_returns_200_OK_when_application_runs();
        metrics_endpoint_returns_200_OK_when_application_runs();
    }

    public static class DataSourceWithoutVaultIntegration implements ApplicationDataSource {
        @Override
        public DataSource dataSource() {
            return DatabaseTestUtils.createPgsqlDatasource(postgresqlContainer);
        }
    }
}
