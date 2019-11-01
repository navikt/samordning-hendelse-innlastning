package no.nav.samordning.innlasting

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import org.testcontainers.containers.Network.newNetwork
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.utility.MountableFile
import java.sql.SQLException
import java.util.*
import javax.sql.DataSource

object DatabaseTestUtils {

    private val network = newNetwork()
    private const val DATABASE_NAME = "samordning-hendelser"
    private const val DATABASE_USERNAME = "postgres"
    private const val DATABASE_PASSWORD = "password"
    private const val LOCALHOST_IP_ADDRESS = "127.0.0.1"
    private const val POSTGRES_INIT_DB_SCRIPT_FOLDER = "/docker-entrypoint-initdb.d/schema.sql"
    private const val INIT_SCRIPT = "schema.sql"
    private const val POSTGRES_PORT = 5432

    internal class SimplePostgreSQLContainer : PostgreSQLContainer<SimplePostgreSQLContainer>()

    fun setUpPostgresContainer(): PostgreSQLContainer<*> = SimplePostgreSQLContainer()
            .withDatabaseName(DATABASE_NAME)
            .withUsername(DATABASE_USERNAME)
            .withPassword(DATABASE_PASSWORD)
            .withNetworkAliases(DATABASE_NAME)
            .withNetwork(network)
            .withExposedPorts(POSTGRES_PORT)
            .withExtraHost("host", LOCALHOST_IP_ADDRESS)
            .withCopyFileToContainer(MountableFile.forClasspathResource(INIT_SCRIPT),
                    POSTGRES_INIT_DB_SCRIPT_FOLDER)

    @Throws(SQLException::class)
    fun getFirstJsonHendelseFromDb(pgsqlDatasource: DataSource): String {

        val jsonHendelse = ArrayList<String>()
        val statement = pgsqlDatasource.connection.createStatement()
        val resultSet = statement.executeQuery("SELECT * FROM HENDELSER LIMIT 1")

        while (resultSet.next()) {
            jsonHendelse.add(resultSet.getString("HENDELSE_DATA"))
        }
        return jsonHendelse.first() //WHY???
    }

    @Throws(SQLException::class)
    fun getFirstTpnrFromDb(pgsqlDatasource: DataSource): String {

        val tpnrList = ArrayList<String>()
        val statement = pgsqlDatasource.connection.createStatement()
        val resultSet = statement.executeQuery("SELECT TPNR FROM HENDELSER LIMIT 1")

        while (resultSet.next()) {
            tpnrList.add(resultSet.getString("TPNR"))
        }
        return tpnrList.first() //WHY???
    }

    fun createPgsqlDatasource(postgreSQLContainer: PostgreSQLContainer<*>) = with(HikariConfig()) {
        maxLifetime = 1000
        connectionTimeout = 250
        jdbcUrl = postgreSQLContainer.getJdbcUrl()
        username = postgreSQLContainer.getUsername()
        password = postgreSQLContainer.getPassword()
        HikariDataSource(this)
    }
}
