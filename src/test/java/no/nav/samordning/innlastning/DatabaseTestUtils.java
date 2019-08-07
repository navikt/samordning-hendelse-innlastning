package no.nav.samordning.innlastning;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.utility.MountableFile;

import javax.sql.DataSource;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class DatabaseTestUtils {

    private static Network network = Network.newNetwork();
    private static final String DATABASE_NAME = "samordning-hendelser";
    private static final String DATABASE_USERNAME = "postgres";
    private static final String DATABASE_PASSWORD = "password";
    private static final String LOCALHOST_IP_ADDRESS = "127.0.0.1";
    private static final String POSTGRES_INIT_DB_SCRIPT_FOLDER = "/docker-entrypoint-initdb.d/schema.sql";
    private static final String INIT_SCRIPT = "schema.sql";
    private static final int POSTGRES_PORT = 5432;


    public static PostgreSQLContainer setUpPostgresContainer() {
        return new PostgreSQLContainer<>()
                .withDatabaseName(DATABASE_NAME)
                .withUsername(DATABASE_USERNAME)
                .withPassword(DATABASE_PASSWORD)
                .withNetworkAliases(DATABASE_NAME)
                .withNetwork(network)
                .withExposedPorts(POSTGRES_PORT)
                .withExtraHost("host", LOCALHOST_IP_ADDRESS)
                .withCopyFileToContainer(MountableFile.forClasspathResource(INIT_SCRIPT),
                        POSTGRES_INIT_DB_SCRIPT_FOLDER);
    }

    public static String getFirstJsonHendelseFromDb(DataSource pgsqlDatasource) throws SQLException {

        List<String> jsonHendelse = new ArrayList<>();
        Statement statement = pgsqlDatasource.getConnection().createStatement();
        ResultSet resultSet = statement.executeQuery("SELECT * FROM HENDELSER LIMIT 1");

        while ( resultSet.next()) {
            jsonHendelse.add(resultSet.getString("HENDELSE_DATA"));
        }
        return jsonHendelse.get(0);
    }

    public static String getFirstTpnrFromDb(DataSource pgsqlDatasource) throws SQLException {

        List<String> tpnrList = new ArrayList<>();
        Statement statement = pgsqlDatasource.getConnection().createStatement();
        ResultSet resultSet = statement.executeQuery("SELECT TPNR FROM HENDELSER LIMIT 1");

        while ( resultSet.next()) {
            tpnrList.add(resultSet.getString("TPNR"));
        }
        return tpnrList.get(0);
    }

    public static HikariDataSource createPgsqlDatasource(PostgreSQLContainer postgreSQLContainer) {
        HikariConfig hikariConfig = new HikariConfig();
        hikariConfig.setMaxLifetime(1000);
        hikariConfig.setConnectionTimeout(250);
        hikariConfig.setJdbcUrl(postgreSQLContainer.getJdbcUrl());
        hikariConfig.setUsername(postgreSQLContainer.getUsername());
        hikariConfig.setPassword(postgreSQLContainer.getPassword());
        return new HikariDataSource(hikariConfig);
    }
}
