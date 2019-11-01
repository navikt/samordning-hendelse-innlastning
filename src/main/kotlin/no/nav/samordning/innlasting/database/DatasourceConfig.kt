package no.nav.samordning.innlasting.database

import com.zaxxer.hikari.HikariConfig

object DatasourceConfig {

    private const val MIN_IDLE = 0
    private const val MAX_LIFETIME_MS = 30001L
    private const val MAX_POOL_SIZE = 3
    private const val CONNECTION_TIMEOUT_MS = 250L
    private const val IDLE_TIMEOUT_MS = 10001L

    fun getDatasourceConfig(dbUrl: String) = HikariConfig().apply {
        jdbcUrl = dbUrl
        minimumIdle = MIN_IDLE
        maxLifetime = MAX_LIFETIME_MS
        maximumPoolSize = MAX_POOL_SIZE
        connectionTimeout = CONNECTION_TIMEOUT_MS
        idleTimeout = IDLE_TIMEOUT_MS
    }
}
