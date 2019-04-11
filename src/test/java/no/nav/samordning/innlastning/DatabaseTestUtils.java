package no.nav.samordning.innlastning;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.images.builder.ImageFromDockerfile;
import org.testcontainers.utility.MountableFile;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class DatabaseTestUtils {

    public static final String DB_MOUNTPATH = "secrets/test";
    public static final String DB_ROLE = "postgres-user";

    private static Network network = Network.newNetwork();
    private static final String VAULT_IMAGE_NAME = "vault:1.1.0";
    private static final String DB_POLICY_CONFIG_FILE = "policy-db.hcl";
    private static final String DATABASE_NAME = "samordning-hendelser";
    private static final String DATABASE_USERNAME = "postgres";
    private static final String DATABASE_PASSWORD = "password";
    private static final String LOCALHOST_IP_ADDRESS = "127.0.0.1";
    private static final String POSTGRES_INIT_DB_SCRIPT_FOLDER = "/docker-entrypoint-initdb.d/schema.sql";
    private static final String INIT_SCRIPT = "schema.sql";
    private static final int POSTGRES_PORT = 5432;
    private static final int VAULT_PORT = 8200;


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

    public static GenericContainer setUpVaultContainer() {
        return new GenericContainer(
                new ImageFromDockerfile()
                        .withDockerfileFromBuilder(builder ->
                                builder.from(VAULT_IMAGE_NAME)
                                       .env("VAULT_ADDR", "http://localhost:" + VAULT_PORT)
                                       .env("VAULT_DEV_ROOT_TOKEN_ID", "secret")
                                       .env("VAULT_TOKEN", "secret")
                                       .build()))
                .withExposedPorts(VAULT_PORT)
                .withNetwork(network)
                .withNetworkAliases("vault")
                .withExtraHost("host", LOCALHOST_IP_ADDRESS)
                .withCopyFileToContainer(MountableFile.forClasspathResource(DB_POLICY_CONFIG_FILE), "/" + DB_POLICY_CONFIG_FILE);
    }

    public static void runVaultContainerCommands(GenericContainer vaultContainer, String postgresContainerIpAddress) throws Exception  {
        System.setProperty("VAULT_ADDR", "http://" + postgresContainerIpAddress + ":" + vaultContainer.getMappedPort(8200));
        System.setProperty("VAULT_TOKEN", "secret");

        vaultContainer.execInContainer("vault", "secrets", "enable", "-path=secrets/test", "database");
        vaultContainer.execInContainer("vault", "write", "secrets/test/config/postgres",
                "allowed_roles=postgres-user",
                "plugin_name=postgresql-database-plugin",
                String.format("connection_url=postgresql://{{username}}:{{password}}@%s:%d/%s?sslmode=disable",
                        DATABASE_NAME, POSTGRES_PORT, DATABASE_NAME),
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

    public static String getFirstJsonHendelseFromDb(HikariDataSource pgsqlDatasource) throws SQLException {

        List<String> jsonHendelse = new ArrayList<>();
        Statement statement = pgsqlDatasource.getConnection().createStatement();
        ResultSet resultSet = statement.executeQuery("SELECT * FROM HENDELSER LIMIT 1");

        while ( resultSet.next()) {
            jsonHendelse.add(resultSet.getString("HENDELSE_DATA"));
        }
        return jsonHendelse.get(0);
    }

    public static HikariDataSource createPgsqlDatasource(PostgreSQLContainer postgreSQLContainer) {
        HikariConfig hikariConfig = new HikariConfig();
        hikariConfig.setJdbcUrl(postgreSQLContainer.getJdbcUrl());
        hikariConfig.setUsername(postgreSQLContainer.getUsername());
        hikariConfig.setPassword(postgreSQLContainer.getPassword());
        return new HikariDataSource(hikariConfig);
    }
}
