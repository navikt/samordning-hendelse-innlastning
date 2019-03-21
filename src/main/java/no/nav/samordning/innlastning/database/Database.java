package no.nav.samordning.innlastning.database;

import org.postgresql.util.PGobject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.json.bind.Jsonb;
import javax.json.bind.JsonbBuilder;
import java.sql.*;
import java.util.Properties;

public class Database {

    private static final Logger LOG = LoggerFactory.getLogger(Database.class);

    private final String INSERT_RECORD_SQL = "INSERT INTO HENDELSER(HENDELSE_DATA) VALUES(to_json(?::json))";

    private Connection connection;

    public Database(String url, String user, String password) {
        connect(url, user, password);
    }

    private void connect(String url, String user, String password) {
        Properties connectionProps = new Properties();
        connectionProps.put("user", user);
        connectionProps.put("password", password);

        try {
            this.connection = DriverManager.getConnection(url, connectionProps);
        } catch (SQLException e) {
            LOG.error("Database access error. Could not connect to " + url, e);
            System.exit(1);
        }
    }

    public void insert(Hendelse hendelse) {
        Jsonb jsonb = JsonbBuilder.create();
        PGobject pGobject = new PGobject();
        pGobject.setType("jsonb");
        try {
            pGobject.setValue(jsonb.toJson(hendelse));
            PreparedStatement insertStatement = connection.prepareStatement(INSERT_RECORD_SQL);
            insertStatement.setObject(1, pGobject, Types.OTHER);
            insertStatement.executeUpdate();
            LOG.info("Inserted: " + hendelse.toString());
        } catch (SQLException e) {
            LOG.error("Insert failed. " + hendelse.toString(), e);
        }
    }
}

