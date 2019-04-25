package no.nav.samordning.innlastning.database;

import com.zaxxer.hikari.HikariConfig;

public class DatasourceConfig {

    private static final int MIN_IDLE = 0;
    private static final int MAX_LIFETIME_MS = 30001;
    private static final int MAX_POOL_SIZE = 3;
    private static final int CONNECTION_TIMEOUT_MS = 250;
    private static final int IDLE_TIMEOUT_MS = 10001;

    public static HikariConfig getDatasourceConfig(String dbUrl) {
        HikariConfig datasourceConfig = new HikariConfig();
        datasourceConfig.setJdbcUrl(dbUrl);
        datasourceConfig.setMinimumIdle(MIN_IDLE);
        datasourceConfig.setMaxLifetime(MAX_LIFETIME_MS);
        datasourceConfig.setMaximumPoolSize(MAX_POOL_SIZE);
        datasourceConfig.setConnectionTimeout(CONNECTION_TIMEOUT_MS);
        datasourceConfig.setIdleTimeout(IDLE_TIMEOUT_MS);
        return datasourceConfig;
    }
}
