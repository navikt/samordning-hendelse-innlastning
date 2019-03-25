package no.nav.samordning.innlastning.database;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import org.postgresql.util.PGobject;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.MountableFile;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import javax.json.bind.Jsonb;
import javax.json.bind.JsonbBuilder;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

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

    private final String INSERT_RECORD_SQL = "INSERT INTO HENDELSER(HENDELSE_DATA) VALUES(to_json(?::json))";
    private final String FETCH_SQL = "SELECT * FROM HENDELSER";

    private static HikariDataSource ds;

    @BeforeAll
    static void createDatasource() {
        HikariConfig hikariConfig = new HikariConfig();
        hikariConfig.setJdbcUrl(postgresqlContainer.getJdbcUrl());
        hikariConfig.setUsername(postgresqlContainer.getUsername());
        hikariConfig.setPassword(postgresqlContainer.getPassword());

        ds = new HikariDataSource(hikariConfig);
    }

    void insert(Hendelse hendelse) throws SQLException {

        Jsonb jsonb = JsonbBuilder.create();
        PGobject pGobject = new PGobject();
        pGobject.setType("jsonb");
        pGobject.setValue(jsonb.toJson(hendelse));

        PreparedStatement insertStatement = ds.getConnection().prepareStatement(INSERT_RECORD_SQL);
        insertStatement.setObject(1, pGobject, Types.OTHER);
        insertStatement.executeUpdate();
    }

    private ResultSet fetch() throws Exception {
        Statement statement = ds.getConnection().createStatement();
        statement.execute(FETCH_SQL);
        ResultSet resultSet = statement.getResultSet();
        return resultSet;
    }

    @Test
    void insert_and_perform_query() throws Exception{
        Hendelse hendelse = new Hendelse();
        hendelse.setVedtakId("JKL678");
        hendelse.setIdentifikator("987654321");
        hendelse.setYtelsesType("AAP");
        hendelse.setFom("2000-01-01");
        hendelse.setTom("2010-02-04");

        insert(hendelse);

        ResultSet resultSet = fetch();

        List<String> excpected = new ArrayList<>();
        List<String> actual = new ArrayList<>();

        excpected.add("{\"fom\": \"2000-01-01\", \"tom\": \"2010-02-04\", \"vedtakId\": \"JKL678\", \"ytelsesType\": \"AAP\", \"identifikator\": \"987654321\"}");

        while ( resultSet.next()) {
            actual.add(resultSet.getString("HENDELSE_DATA"));
        }

        assertEquals(excpected, actual);
    }
}
