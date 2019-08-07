package no.nav.samordning.innlastning.database;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;
import javax.sql.DataSource;

import org.postgresql.util.PGobject;

public class Database {

    private static final Logger LOG = LoggerFactory.getLogger(Database.class);

    private static final String INSERT_RECORD_SQL = "INSERT INTO HENDELSER(HENDELSE_DATA) VALUES(to_json(?::json))";

    private final DataSource dataSource;
    private static final String POSTGRES_OBJECT_TYPE = "jsonb";

    public Database(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void insert(String hendelseJson, String tpnr) {
        try (Connection connection = dataSource.getConnection()) {
            PGobject jsonbObject = createJsonbObject(hendelseJson);
            PreparedStatement insertStatement = connection.prepareStatement(INSERT_RECORD_SQL);
            insertStatement.setObject(1, jsonbObject, Types.OTHER);
            insertStatement.executeUpdate();
            LOG.info("Inserted: {}", hendelseJson);
        } catch (SQLException e) {
            throw new FailedInsert(hendelseJson, e);
        }
    }

    private PGobject createJsonbObject(String hendelseJson) throws SQLException {
        PGobject pgObject = new PGobject();
        pgObject.setType(POSTGRES_OBJECT_TYPE);
        pgObject.setValue(hendelseJson);
        return pgObject;
    }
}

