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

    private final String INSERT_RECORD_SQL = "INSERT INTO HENDELSER(HENDELSE_DATA) VALUES(to_json(?::json))";

    private HikariDataSource dataSource;

    public Database(String url, String mountPath, String role) throws VaultError {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(url);
        config.setMinimumIdle(0);
        config.setMaxLifetime(30001);
        config.setMaximumPoolSize(2);
        config.setConnectionTimeout(250);
        config.setIdleTimeout(10001);
        dataSource = HikariCPVaultUtil.createHikariDataSourceWithVaultIntegration(config, mountPath, role);
    }

    public void insert(Hendelse hendelse) {
        Jsonb jsonb = JsonbBuilder.create();
        PGobject pGobject = new PGobject();
        pGobject.setType("jsonb");
        try {
            Connection connection = dataSource.getConnection();
            pGobject.setValue(jsonb.toJson(hendelse));
            PreparedStatement insertStatement = connection.prepareStatement(INSERT_RECORD_SQL);
            insertStatement.setObject(1, pGobject, Types.OTHER);
            insertStatement.executeUpdate();
            LOG.info("Inserted: " + hendelse.toString());
            connection.close();
        } catch (SQLException e) {
            LOG.error("Insert failed. " + hendelse.toString(), e);
        }
    }
}

