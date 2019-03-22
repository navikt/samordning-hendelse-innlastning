package no.nav.samordning.innlastning.database;

import org.junit.jupiter.api.Test;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.MountableFile;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.sql.ResultSet;
import java.sql.Statement;

@Testcontainers
class DatabaseTest {
    private static final String DATABASE_NAME = "samordninghendelser";
    private static final String DATABASE_USERNAME = "samordninghendelser";
    private static final String DATABASE_PASSWORD = "samordninghendelser";

    @Container
    private static PostgreSQLContainer postgresqlContainer = new PostgreSQLContainer<>()
            .withDatabaseName(DATABASE_NAME)
            .withUsername(DATABASE_USERNAME)
            .withPassword(DATABASE_PASSWORD)
            .withCopyFileToContainer(MountableFile.forClasspathResource("schema.sql"),
                    "/docker-entrypoint-initdb.d/");

    @Test
    void insert() throws Exception{

    }

    @Test
    void fetch() throws Exception{

    }

    private ResultSet performQuery(String sql) throws Exception {
        HikariConfig hikariConfig = new HikariConfig();
        hikariConfig.setJdbcUrl(postgresqlContainer.getJdbcUrl());
        hikariConfig.setUsername(postgresqlContainer.getUsername());
        hikariConfig.setPassword(postgresqlContainer.getPassword());

        HikariDataSource ds = new HikariDataSource(hikariConfig);
        Statement statement = ds.getConnection().createStatement();
        statement.execute(sql);
        ResultSet resultSet = statement.getResultSet();
        resultSet.next();
        return resultSet;
    }
}
