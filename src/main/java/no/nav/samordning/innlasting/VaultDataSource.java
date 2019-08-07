package no.nav.samordning.innlasting;

import com.zaxxer.hikari.HikariConfig;
import no.nav.samordning.innlasting.database.DatasourceConfig;
import no.nav.vault.jdbc.hikaricp.VaultError;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.util.Map;

import static no.nav.samordning.innlasting.ApplicationProperties.getFromEnvironment;
import static no.nav.vault.jdbc.hikaricp.HikariCPVaultUtil.createHikariDataSourceWithVaultIntegration;

public class VaultDataSource implements Application.ApplicationDataSource {

    private static final Logger LOG = LoggerFactory.getLogger(VaultDataSource.class);

    private static final String DB_URL_ENV_KEY = "DB_URL";
    private static final String DB_MOUNT_PATH_ENV_KEY = "DB_MOUNT_PATH";
    private static final String DB_ROLE_ENV_KEY = "DB_ROLE";

    private DataSource dataSource;

    VaultDataSource(Map<String, String> env) {
        setVaultDataSource(env);
    }

    private void setVaultDataSource(Map<String, String> env) {
        String jdbcUrl = getFromEnvironment(env, DB_URL_ENV_KEY);
        String mountPath = getFromEnvironment(env, DB_MOUNT_PATH_ENV_KEY);
        String role = getFromEnvironment(env, DB_ROLE_ENV_KEY);

        HikariConfig datasourceConfig = DatasourceConfig.getDatasourceConfig(jdbcUrl);

        try {
            this.dataSource = createHikariDataSourceWithVaultIntegration(datasourceConfig, mountPath, role);
        } catch (VaultError vaultError) {
            LOG.error("Database access error. Could not connect to " + jdbcUrl, vaultError);
            System.exit(1);
        }
    }

    @Override
    public DataSource dataSource() {
        return this.dataSource;
    }
}
