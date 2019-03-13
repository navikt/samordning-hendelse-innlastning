package no.nav.samordning.innlastning;

import org.postgresql.util.PGobject;

import javax.json.bind.Jsonb;
import javax.json.bind.JsonbBuilder;
import java.sql.*;
import java.util.Properties;

class Database {

    private final String SQL_INSERT_RECORD = "INSERT INTO T_SAMORDNINGSPLIKTIG_VEDTAK VALUES(to_json(?::json))";

    private Connection conn= null;

    Connection getConnection(String url, String user, String password) throws SQLException {
        Properties connectionProps = new Properties();
        connectionProps.put("user", user);
        connectionProps.put("password", password);

        conn = DriverManager.getConnection(
                url,
                connectionProps);

        return conn;
    }

    void insert(Hendelse hendelse) throws SQLException{
        Jsonb jsonb = JsonbBuilder.create();
        PGobject pGobject = new PGobject();
        pGobject.setType("jsonb");
        pGobject.setValue(jsonb.toJson(hendelse));

        try {
            PreparedStatement insertStatement = conn.prepareStatement(SQL_INSERT_RECORD);
            insertStatement.setObject(1, pGobject, Types.OTHER);
            insertStatement.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Could not perform mapping");
        }
    }
}

