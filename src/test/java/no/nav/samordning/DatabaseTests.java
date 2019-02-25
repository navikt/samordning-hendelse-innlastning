package no.nav.samordning;

import org.junit.ClassRule;
import org.junit.Test;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.utility.MountableFile;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class DatabaseTests {

    @ClassRule
   public static final PostgreSQLContainer postgresContainer = new PostgreSQLContainer<>("postgres")
            .withCopyFileToContainer(MountableFile.forClasspathResource("schema.sql"), "/docker-entrypoint-initdb.d/")
            .withDatabaseName("db")
            .withPassword("pass")
            .withUsername("user");

    @Test
    public void write_To_And_Read_From_DB_In_Docker_Container() throws SQLException, ClassNotFoundException {
        Class.forName("org.postgresql.Driver");

        Connection conn = null;
        Properties connectionProps = new Properties();
        connectionProps.put("user", postgresContainer.getUsername());
        connectionProps.put("password", postgresContainer.getPassword());

        conn = DriverManager.getConnection(
                postgresContainer.getJdbcUrl(),
                connectionProps);

        Statement insertStatement= conn.createStatement();
        String sqlInsert = "INSERT INTO T_SAMORDNINGSPLIKTIG_VEDTAK VALUES('{\"identifikator\": \"23456789012\", " +
                "\"ytelsesType\": \"AAP\", " +
                "\"vedtakId\": \"123ABC\", " +
                "\"fom\": \"2021-02-02\"}');";
        insertStatement.execute(sqlInsert);

        Statement queryStatement= conn.createStatement();
        String sqlQuery = "Select * FROM T_SAMORDNINGSPLIKTIG_VEDTAK";
        ResultSet rs = queryStatement.executeQuery(sqlQuery);

        List<String> excpected = new ArrayList();
        List<String> actual = new ArrayList();

        excpected.add("{\"fom\": \"2020-01-01\", \"vedtakId\": \"ABC123\", \"ytelsesType\": \"AAP\", \"identifikator\": \"12345678901\"}");
        excpected.add("{\"fom\": \"2021-02-02\", \"vedtakId\": \"123ABC\", \"ytelsesType\": \"AAP\", \"identifikator\": \"23456789012\"}");

        while (rs.next()) {
            actual.add(rs.getString("DATA"));
        }

        conn.close();

        assertEquals(excpected, actual);
    }
}
