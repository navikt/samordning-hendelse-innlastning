package no.nav.samordning.innlastning.database;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import no.nav.vault.jdbc.hikaricp.HikariCPVaultUtil;
import no.nav.vault.jdbc.hikaricp.VaultError;
import org.postgresql.util.PGobject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.json.bind.Jsonb;
import javax.json.bind.JsonbBuilder;
import java.sql.*;

public class Database {

    private static final Logger LOG = LoggerFactory.getLogger(Database.class);

    private static final int MIN_IDLE = 0;
    private static final int MAX_LIFETIME_MS = 30001;
    private static final int MAX_POOL_SIZE = 2;
    private static final int CONNECTION_TIMEOUT_MS = 250;
    private static final int IDLE_TIMEOUT_MS = 10001;

    private final String INSERT_RECORD_SQL = "INSERT INTO HENDELSER(HENDELSE_DATA) VALUES(to_json(?::json))";

    private HikariDataSource dataSource;

    public Database(String dbUrl, String mountPath, String role) throws VaultError {
        HikariConfig config = getDatasourceConfig(dbUrl);
        this.dataSource = HikariCPVaultUtil.createHikariDataSourceWithVaultIntegration(config, mountPath, role);
    }

    public void insert(Hendelse hendelse) {
        Jsonb jsonb = JsonbBuilder.create();
        PGobject pGobject = new PGobject();
        pGobject.setType("jsonb");

        try(Connection connection = dataSource.getConnection()) {
            pGobject.setValue(jsonb.toJson(hendelse));
            PreparedStatement insertStatement = connection.prepareStatement(INSERT_RECORD_SQL);
            insertStatement.setObject(1, pGobject, Types.OTHER);
            insertStatement.executeUpdate();
            LOG.info("Inserted: " + hendelse.toString());
        } catch (SQLException e) {
            throw new FailedInsert(hendelse.toString(), e);
        }
    }

    private HikariConfig getDatasourceConfig(String dbUrl) {
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

