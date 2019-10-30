package no.nav.samordning.innlasting

import no.nav.samordning.innlasting.ApplicationProperties.getFromEnvironment
import no.nav.samordning.innlasting.database.DatasourceConfig
import no.nav.vault.jdbc.hikaricp.HikariCPVaultUtil.createHikariDataSourceWithVaultIntegration
import no.nav.vault.jdbc.hikaricp.VaultError
import org.slf4j.LoggerFactory
import javax.sql.DataSource
import kotlin.system.exitProcess

class VaultDataSource internal constructor(env: Map<String, String>) : Application.ApplicationDataSource {

    private var dataSource: DataSource = {
        val jdbcUrl = getFromEnvironment(env, DB_URL_ENV_KEY)
        val mountPath = getFromEnvironment(env, DB_MOUNT_PATH_ENV_KEY)
        val role = getFromEnvironment(env, DB_ROLE_ENV_KEY)

        val datasourceConfig = DatasourceConfig.getDatasourceConfig(jdbcUrl)

        try {
            createHikariDataSourceWithVaultIntegration(datasourceConfig, mountPath, role)
        } catch (vaultError: VaultError) {
            LOG.error("Database access error. Could not connect to $jdbcUrl", vaultError)
            exitProcess(1)
        }
    }()

    override fun dataSource() = dataSource

    companion object {

        private val LOG = LoggerFactory.getLogger(VaultDataSource::class.java)

        private const val DB_URL_ENV_KEY = "DB_URL"
        private const val DB_MOUNT_PATH_ENV_KEY = "DB_MOUNT_PATH"
        private const val DB_ROLE_ENV_KEY = "DB_ROLE"
    }
}
