package no.nav.samordning.innlastning.database;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.images.builder.ImageFromDockerfile;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.MountableFile;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Testcontainers
class DatabaseTest {
    private static Network network = Network.newNetwork();
    private static final String DATABASE_NAME = "samordning-hendelser";
    private static final String DATABASE_USERNAME = "postgres";
    private static final String DATABASE_PASSWORD = "password";

    @Container
    private static PostgreSQLContainer postgresqlContainer = new PostgreSQLContainer<>()
            .withDatabaseName(DATABASE_NAME)
            .withUsername(DATABASE_USERNAME)
            .withPassword(DATABASE_PASSWORD)
            .withNetworkAliases(DATABASE_NAME)
            .withNetwork(network)
            .withExposedPorts(5432)
            .withExtraHost("host", "127.0.0.1")
            .withCopyFileToContainer(MountableFile.forClasspathResource("schema.sql"),
                    "/docker-entrypoint-initdb.d/");

    @Container
    private static GenericContainer vaultContainer = new GenericContainer(
            new ImageFromDockerfile()
                    .withDockerfileFromBuilder(builder ->
                            builder
                                    .from("vault:1.1.0")
                                    .env("VAULT_ADDR", "http://localhost:8200")
                                    .env("VAULT_DEV_ROOT_TOKEN_ID", "secret")
                                    .env("VAULT_TOKEN", "secret")
                                    .build()))
            .withExposedPorts(8200)
            .withNetwork(network)
            .withNetworkAliases("vault")
            .withExtraHost("host", "127.0.0.1")
            .withCopyFileToContainer(MountableFile.forClasspathResource("policy-db.hcl"), "/");

    @BeforeAll
    public static void setup() throws Exception {
        System.setProperty("VAULT_ADDR", "http://localhost:" + vaultContainer.getMappedPort(8200));
        System.setProperty("VAULT_TOKEN", "secret");
        vaultContainer.execInContainer("vault", "secrets", "enable", "-path=secrets/test", "database");
        vaultContainer.execInContainer("vault", "write", "secrets/test/config/postgres",
                "allowed_roles=postgres-user",
                "plugin_name=postgresql-database-plugin",
                String.format("connection_url=postgresql://{{username}}:{{password}}@%s:%d/%s?sslmode=disable",
                        DATABASE_NAME, 5432, DATABASE_NAME),
                "username=" + DATABASE_USERNAME,
                "password=" + DATABASE_PASSWORD);
        vaultContainer.execInContainer("vault", "write", "secrets/test/roles/postgres-user",
                "db_name=postgres",
                "creation_statements=CREATE ROLE \"{{name}}\" WITH SUPERUSER LOGIN PASSWORD \'{{password}}\' " +
                        "VALID UNTIL \'{{expiration}}\';",
                "default_ttl=1m",
                "max_ttl=1m");
        vaultContainer.execInContainer("vault", "policy", "write", "db", "policy-db.hcl");
        vaultContainer.execInContainer("vault", "token", "create", "-policy=db", "-ttl=768h", "-period=15s",
                "-field", "token");
    }

    @Test
    void insert_and_perform_query() throws Exception{
        Hendelse hendelse = new Hendelse();
        hendelse.setVedtakId("JKL678");
        hendelse.setIdentifikator("987654321");
        hendelse.setYtelsesType("AAP");
        hendelse.setFom("2000-01-01");
        hendelse.setTom("2010-02-04");

        HikariDataSource ds = createDatasource();

        Database db = new Database(postgresqlContainer.getJdbcUrl(), "secrets/test", "postgres-user");
        db.insert(hendelse);

        Statement statement = ds.getConnection().createStatement();
        ResultSet resultSet = statement.executeQuery("SELECT * FROM HENDELSER");

        List<String> excpected = new ArrayList<>();
        List<String> actual = new ArrayList<>();

        excpected.add("{\"fom\": \"2000-01-01\", \"tom\": \"2010-02-04\", \"vedtakId\": \"JKL678\", \"ytelsesType\": \"AAP\", \"identifikator\": \"987654321\"}");

        while ( resultSet.next()) {
            actual.add(resultSet.getString("HENDELSE_DATA"));
        }

        assertEquals(excpected, actual);
    }

    private HikariDataSource createDatasource() {
        HikariConfig hikariConfig = new HikariConfig();
        hikariConfig.setJdbcUrl(postgresqlContainer.getJdbcUrl());
        hikariConfig.setUsername(postgresqlContainer.getUsername());
        hikariConfig.setPassword(postgresqlContainer.getPassword());
        return new HikariDataSource(hikariConfig);
    }
}
