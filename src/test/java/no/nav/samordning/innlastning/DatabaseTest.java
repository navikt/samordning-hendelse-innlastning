package no.nav.samordning.innlastning;

import org.junit.ClassRule;
import org.junit.Test;
import org.junit.jupiter.api.AfterAll;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.utility.MountableFile;

import java.sql.*;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class DatabaseTest {

    private static Connection connection;
    private static Database database;

    @ClassRule
    public static final PostgreSQLContainer postgresContainer = new PostgreSQLContainer<>("postgres")
            .withCopyFileToContainer(MountableFile.forClasspathResource("schema.sql"), "/docker-entrypoint-initdb.d/");

    @AfterAll
    public static void tearDown() throws Exception{
        connection.close();
    }

    @Test
    public void write_hendelse_to_database() throws Exception {
        database = new Database();
        connection = database.getConnection(
                postgresContainer.getJdbcUrl(),
                postgresContainer.getUsername(),
                postgresContainer.getPassword()
        );

        Hendelse hendelse = new Hendelse();
        hendelse.setYtelsesType("AAP");
        hendelse.setIdentifikator("23456789012");
        hendelse.setVedtakId("123ABC");
        hendelse.setFom("2021-02-02");
        hendelse.setTom(null);

        database.insert(hendelse);

        String expected = "{\"fom\": \"2021-02-02\", \"vedtakId\": \"123ABC\", \"ytelsesType\": \"AAP\", \"identifikator\": \"23456789012\"}";

        String insertedHendelse = fetchFirstRecord();

        assertEquals(expected, insertedHendelse);
    }

    private String fetchFirstRecord() throws Exception {
        Statement queryStatement = connection.createStatement();

        String sqlQuery = "SELECT * " +
                "FROM T_SAMORDNINGSPLIKTIG_VEDTAK " +
                "LIMIT 1";

        ResultSet resultSet = queryStatement.executeQuery(sqlQuery);
        resultSet.next();

        return resultSet.getString("DATA");
    }
}
