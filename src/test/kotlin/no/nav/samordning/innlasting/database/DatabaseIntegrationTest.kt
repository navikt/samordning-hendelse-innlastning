package no.nav.samordning.innlasting.database

import no.nav.samordning.innlasting.DatabaseTestUtils.createPgsqlDatasource
import no.nav.samordning.innlasting.DatabaseTestUtils.getFirstJsonHendelseFromDb
import no.nav.samordning.innlasting.DatabaseTestUtils.getFirstTpnrFromDb
import no.nav.samordning.innlasting.DatabaseTestUtils.setUpPostgresContainer
import no.nav.samordning.schema.SamordningHendelse
import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import org.testcontainers.shaded.com.fasterxml.jackson.databind.ObjectMapper

@Testcontainers
@TestMethodOrder(MethodOrderer.OrderAnnotation::class)
internal class DatabaseIntegrationTest {

    private val testHendelseJson: String
        @Throws(Exception::class)
        get() = with(SamordningHendelse().run {
            setVedtakId(VEDTAK_ID)
            setIdentifikator(IDENTIFIKATOR)
            setYtelsesType(YTELSES_TYPE)
            setFom(FOM)
            setTom(TOM)
            toString()
        }, objectMapper::writeValueAsString)

    @Test
    @Order(1)
    @Throws(Exception::class)
    fun hendelse_inserted_to_db_as_json() {

        val hendelseJson = testHendelseJson
        database.insert(hendelseJson, TPNR)

        val expectedHendelse = """{"identifikator": "$IDENTIFIKATOR", "ytelsesType": "$YTELSES_TYPE", "vedtakId": "$VEDTAK_ID", "fom": "$FOM", "tom": "$TOM"}"""
        val expectedHendelseJson = objectMapper.writeValueAsString(expectedHendelse)

        val pgsqlDatasource = createPgsqlDatasource(postgresqlContainer)
        val actualHendelse = getFirstJsonHendelseFromDb(pgsqlDatasource)
        val actualTpnr = getFirstTpnrFromDb(pgsqlDatasource)

        assertEquals(expectedHendelseJson, actualHendelse)
        assertEquals(TPNR, actualTpnr)
    }

    @Test
    @Order(2)
    fun insert_fails_when_database_is_down() {

        breakDatabaseConnection()

        assertThrows(
                FailedInsert::class.java
        ) { database.insert(testHendelseJson, TPNR) }
    }

    private fun breakDatabaseConnection() = postgresqlContainer.stop()

    companion object {

        private const val TPNR = "1234"
        private const val VEDTAK_ID = "JKL678"
        private const val IDENTIFIKATOR = "987654321"
        private const val YTELSES_TYPE = "AAP"
        private const val FOM = "2000-01-01"
        private const val TOM = "2010-02-04"

        private lateinit var database: Database
        private val objectMapper = ObjectMapper()

        @Container
        private val postgresqlContainer = setUpPostgresContainer()

        @BeforeAll
        @JvmStatic
        fun setUp() {
            val datasource = createPgsqlDatasource(postgresqlContainer)
            database = Database(datasource)
        }
    }
}
