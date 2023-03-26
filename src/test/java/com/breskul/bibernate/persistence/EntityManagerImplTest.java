package com.breskul.bibernate.persistence;

import com.breskul.bibernate.exeptions.JdbcDaoException;
import com.breskul.bibernate.persistence.testModel.Note;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.*;
import org.postgresql.ds.PGSimpleDataSource;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.function.Consumer;

@TestClassOrder(ClassOrderer.OrderAnnotation.class)
public class EntityManagerImplTest {

    private static final String CREATE_TABLE = "create table if not exists notes(id bigint primary key,body varchar(255));";
    private static final String DROP_TABLE = "drop table if exists  notes;";
    private static final String NEW_NOTE_QUERY = "insert into notes (id, body) values (1, 'my note')";
    private static final String DB_URL = "jdbc:postgresql://localhost:5432/postgres";
    private static DataSource dataSource;

    @BeforeAll
    public static void initDataSource() {
        PGSimpleDataSource pgSimpleDataSource = new PGSimpleDataSource();
        pgSimpleDataSource.setURL(DB_URL);
        dataSource = pgSimpleDataSource;
        doInConnection(connection -> {
            try {
                PreparedStatement createTable = connection.prepareStatement(CREATE_TABLE);
                createTable.execute();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        });
    }

    private static void doInConnection(Consumer<Connection> consumer) {
        try (Connection connection = dataSource.getConnection()) {
            consumer.accept(connection);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @AfterAll
    public static void dropTable() {
        doInConnection(connection -> {
            try {
                PreparedStatement dropTable = connection.prepareStatement(DROP_TABLE);
                dropTable.execute();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        });
    }

    @Test
    @Order(1)
    @DisplayName("1. Test remove method")
    public void testRemoveMethod() {
        EntityManager entityManager = new EntityMangerImpl(dataSource);
        Note note = new Note();
        note.setId(1L);
        note.setBody("My note");
        Assertions.assertThrows(JdbcDaoException.class, () -> entityManager.remove(note));

        doInConnection(connection -> {
            try {
                PreparedStatement newNote = connection.prepareStatement(NEW_NOTE_QUERY);
                newNote.execute();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        });
        Assertions.assertDoesNotThrow(() -> entityManager.remove(note));

        entityManager.close();
    }


}
