package no.nav.samordning.innlastning.database;

import org.postgresql.util.PGobject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.json.bind.Jsonb;
import javax.json.bind.JsonbBuilder;
import javax.sql.DataSource;
import java.sql.*;

public class Database {

    private static final Logger LOG = LoggerFactory.getLogger(Database.class);

    private final String INSERT_RECORD_SQL = "INSERT INTO HENDELSER(HENDELSE_DATA) VALUES(to_json(?::json))";

    private DataSource dataSource;

    public Database(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void insert(Hendelse hendelse) {
        Jsonb jsonb = JsonbBuilder.create();
        PGobject pGobject = new PGobject();
        pGobject.setType("jsonb");

        try(Connection connection = dataSource.getConnection()) {
            pGobject.setValue(jsonb.toJson(hendelse));
            PreparedStatement insertStatement = connection.prepareStatement(INSERT_RECORD_SQL);
            insertStatement.setObject(1, pGobject, Types.OTHER);
            insertStatement.executeUpdate();
            LOG.info("Inserted: " + hendelse.toString());
        } catch (SQLException e) {
            throw new FailedInsert(hendelse.toString(), e);
        }
    }
}

