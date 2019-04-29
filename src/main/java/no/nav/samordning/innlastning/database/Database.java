package no.nav.samordning.innlastning.database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import javax.sql.DataSource;
import org.postgresql.util.PGobject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Database {

    private static final Logger LOG = LoggerFactory.getLogger(Database.class);

    private static final String INSERT_RECORD_SQL = "INSERT INTO HENDELSER(HENDELSE_DATA) VALUES(to_json(?::json))";
    private final ObjectMapper objectMapper;

    private DataSource dataSource;

    public Database(DataSource dataSource) {
        this.dataSource = dataSource;
        this.objectMapper = new ObjectMapper();
    }

    public void insert(Hendelse hendelse) {
        String hendelseString = hendelse.toString();
        PGobject pGobject = new PGobject();
        pGobject.setType("jsonb");

        try (Connection connection = dataSource.getConnection()) {
            pGobject.setValue(objectMapper.writeValueAsString(hendelse));
            PreparedStatement insertStatement = connection.prepareStatement(INSERT_RECORD_SQL);
            insertStatement.setObject(1, pGobject, Types.OTHER);
            insertStatement.executeUpdate();
            LOG.info("Inserted: {}", hendelseString);
        } catch (SQLException | JsonProcessingException e) {
            throw new FailedInsert(hendelseString, e);
        }
    }
}

