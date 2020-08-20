package no.nav.samordning.innlasting.database

import org.postgresql.util.PGobject
import org.slf4j.LoggerFactory
import java.sql.SQLException
import java.sql.Types
import javax.sql.DataSource

class Database(private val dataSource: DataSource) {

    fun insert(hendelseJson: String, tpnr: String) {
        try {
            dataSource.connection.use {
                val jsonbObject = createJsonbObject(hendelseJson)
                val insertStatement = it.prepareStatement(INSERT_STATEMENT)
                insertStatement.setObject(1, tpnr, Types.VARCHAR)
                insertStatement.setObject(2, jsonbObject, Types.OTHER)
                insertStatement.executeUpdate()
                LOG.info("Inserted: tpnr: {} and hendelse: {}", tpnr, maskFnrFromLogs(hendelseJson))
            }
        } catch (e: SQLException) {
            throw FailedInsert(hendelseJson, e)
        }
    }

    private fun maskFnrFromLogs(hendelseJson: String) = hendelseJson.replaceRange(PERSONNUMMER_START_INDEX, PERSONNUMMER_END_INDEX, "*****")

    @Throws(SQLException::class)
    private fun createJsonbObject(hendelseJson: String) =
            PGobject().apply {
                type = POSTGRES_OBJECT_TYPE
                value = hendelseJson
            }

    companion object {
        private val LOG = LoggerFactory.getLogger(Database::class.java)
        private const val INSERT_STATEMENT = "INSERT INTO HENDELSER(TPNR, HENDELSE_DATA) VALUES(?, to_json(?::json))"
        private const val POSTGRES_OBJECT_TYPE = "jsonb"

        private const val PERSONNUMMER_START_INDEX = 28
        private const val PERSONNUMMER_END_INDEX = 34
    }
}

