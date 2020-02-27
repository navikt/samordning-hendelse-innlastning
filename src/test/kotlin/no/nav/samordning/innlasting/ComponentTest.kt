package no.nav.samordning.innlasting

import no.nav.samordning.innlasting.Application.ApplicationDataSource
import no.nav.samordning.innlasting.DatabaseTestUtils.createPgsqlDatasource
import no.nav.samordning.innlasting.DatabaseTestUtils.getFirstJsonHendelseFromDb
import no.nav.samordning.innlasting.DatabaseTestUtils.getFirstTpnrFromDb
import no.nav.samordning.innlasting.DatabaseTestUtils.setUpPostgresContainer
import no.nav.samordning.innlasting.KafkaTestEnvironment.kafkaConfiguration
import no.nav.samordning.innlasting.KafkaTestEnvironment.populate_hendelse_topic
import no.nav.samordning.innlasting.KafkaTestEnvironment.setupKafkaEnvironment
import no.nav.samordning.innlasting.NaisEndpointTest.isAlive_endpoint_returns_200_OK_when_application_runs
import no.nav.samordning.innlasting.NaisEndpointTest.isReady_endpoint_returns_200_OK_when_application_runs
import no.nav.samordning.innlasting.NaisEndpointTest.metrics_endpoint_returns_200_OK_when_application_runs
import no.nav.samordning.schema.SamordningHendelse
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import org.testcontainers.shaded.com.fasterxml.jackson.databind.ObjectMapper
import java.lang.Thread.sleep


@Testcontainers
internal class ComponentTest {

    init {
        System.setProperty("zookeeper.jmx.log4j.disable", "TRUE")
        setupKafkaEnvironment()
        app = Application(DataSourceWithoutVaultIntegration(), kafkaConfiguration)
        app.run()
    }

    @Test
    @Throws(Exception::class)
    fun innlasting_reads_hendelser_from_kafka_and_persists_hendelse_to_db() {
        val samordningHendelse = SamordningHendelse(IDENTIFIKATOR, YTELSESTYPE, VEDTAK_ID, SAM_ID, FOM, TOM)

        val expectedHendelse = ObjectMapper().writeValueAsString("""{"identifikator": "$IDENTIFIKATOR", "ytelsesType": "$YTELSESTYPE", "vedtakId": "$VEDTAK_ID", "samId": "$SAM_ID", "fom": "$FOM", "tom": "$TOM"}""")

        populate_hendelse_topic(TPNR, samordningHendelse)

        //Application needs to process records before the tests resume
        sleep(5000L)

        nais_platform_prerequisites_runs_OK()

        val postgresqlDatasource = createPgsqlDatasource(postgresqlContainer)
        val actualHendelse = getFirstJsonHendelseFromDb(postgresqlDatasource)
        val actualTpnr = getFirstTpnrFromDb(postgresqlDatasource)

        assertEquals(expectedHendelse, actualHendelse)
        assertEquals(TPNR, actualTpnr)
    }

    @Throws(Exception::class)
    private fun nais_platform_prerequisites_runs_OK() {
        isAlive_endpoint_returns_200_OK_when_application_runs()
        isReady_endpoint_returns_200_OK_when_application_runs()
        metrics_endpoint_returns_200_OK_when_application_runs()
    }

    class DataSourceWithoutVaultIntegration : ApplicationDataSource {
        override fun dataSource() = createPgsqlDatasource(postgresqlContainer)
    }

    companion object {

        private const val TPNR = "1234"
        private const val IDENTIFIKATOR = "12345678901"
        private const val YTELSESTYPE = "AP"
        private const val VEDTAK_ID = "ABC123"
        private const val SAM_ID = "BOGUS"
        private const val FOM = "01-01-2020"
        private const val TOM = "01-01-2010"

        @Container
        private val postgresqlContainer = setUpPostgresContainer()

        private lateinit var app: Application

        @AfterAll
        @JvmStatic
        fun tearDown() = app.shutdown()
    }
}