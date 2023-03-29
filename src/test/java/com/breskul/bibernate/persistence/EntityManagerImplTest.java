package com.breskul.bibernate.persistence;

import com.breskul.bibernate.AbstractDataSourceTest;
import com.breskul.bibernate.exception.JdbcDaoException;
import com.breskul.bibernate.persistence.testmodel.NoteComplex;
import com.breskul.bibernate.persistence.testmodel.Person;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.*;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.assertEquals;

@TestClassOrder(ClassOrderer.OrderAnnotation.class)
public class EntityManagerImplTest extends AbstractDataSourceTest {

    private static final String INSERT_NEW_PERSON_QUERY_PATTERN = "INSERT INTO users (id, first_name, last_name, birthday) VALUES (%d, '%s', '%s', ?)";
    private static final String SELECT_PERSON_BY_ID = "SELECT * FROM users WHERE id = %s";
    private static final String INSERT_NEW_NOTE_QUERY_PATTERN = "INSERT INTO notes (id, body, person_id, created_at) VALUES (%d, '%s', %d, ?)";

    private EntityManager entityManager;

    @BeforeEach
    void setUp() {
        entityManager = new EntityManagerImpl(dataSource);
    }

    @Test
    @Order(1)
    @DisplayName("1. Test remove method")
    public void testRemoveMethod() {
        Person person = new Person();
        person.setId(10L);
        person.setFirstName("user");
        Assertions.assertThrows(JdbcDaoException.class, () -> entityManager.remove(person));

        var query = String.format(INSERT_NEW_PERSON_QUERY_PATTERN, 10, "Cat", "Schrödinger");
        doInConnection(connection -> {
            try {
                PreparedStatement newPerson = connection.prepareStatement(query);
                newPerson.setObject(1, null);
                newPerson.execute();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        });
        Assertions.assertDoesNotThrow(() -> entityManager.remove(person));

        entityManager.close();
    }

    @Test
    @Order(2)
    @DisplayName("2. Test find method for Entity with OneToMany relation")
    public void testFindMethodWithOneToManyRelation() {
        long personId = 20L;
        Person person = new Person();
        person.setId(personId);
        person.setFirstName("user");
        Assertions.assertThrows(JdbcDaoException.class, () -> entityManager.find(Person.class, personId));

        var firstName = "Tom";
        var lastName = "Hanks";
        var birthday = LocalDateTime.of(1956, Month.JULY, 9, 10,0,0);
        var zonedDateTime = ZonedDateTime.of(birthday, ZoneId.systemDefault());
        long date = zonedDateTime.toInstant().toEpochMilli();
        var query = String.format(INSERT_NEW_PERSON_QUERY_PATTERN, personId, firstName, lastName);

        doInConnection(connection -> {
            try {
                PreparedStatement newPerson = connection.prepareStatement(query);
                newPerson.setObject(1, new Timestamp(date));
                newPerson.execute();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        });
        Assertions.assertThrows(JdbcDaoException.class, () -> entityManager.find(Person.class, 10L));
        Assertions.assertDoesNotThrow(() -> entityManager.find(Person.class, personId));
        var selectedPerson = entityManager.find(Person.class, personId);
        assertEquals(firstName, selectedPerson.getFirstName());
        assertEquals(lastName, selectedPerson.getLastName());
        assertEquals(birthday.toLocalDate(), selectedPerson.getBirthday());
        Assertions.assertDoesNotThrow(() -> entityManager.remove(selectedPerson));
        Assertions.assertThrows(JdbcDaoException.class, () -> entityManager.find(Person.class, personId));
        entityManager.close();
    }

    @Disabled
    @Test
    @Order(3)
    @DisplayName("3. Test find method for Entity with ManyToOne relation")
    public void testFindMethodWithManyToOneRelation() {
        long noteId = 30L;
        NoteComplex noteComplex = new NoteComplex();
        noteComplex.setId(noteId);
        noteComplex.setBody("note");
        Assertions.assertThrows(JdbcDaoException.class, () -> entityManager.find(NoteComplex.class, 30L));

        var firstName = "Keanu";
        var lastName = "Reeves";
        long personId = 30L;
        var birthday = LocalDateTime.of(1964, Month.SEPTEMBER, 2, 10,0,0);
        var birthdayZonedDateTime = ZonedDateTime.of(birthday, ZoneId.systemDefault());
        long birthdayLong = birthdayZonedDateTime.toInstant().toEpochMilli();
        var personInsertQuery = String.format(INSERT_NEW_PERSON_QUERY_PATTERN, personId, firstName, lastName);

        var body = "Pay the main role in the movie John Wick";
        var createdAt = LocalDateTime.now();
        var zonedDateTime = ZonedDateTime.of(createdAt, ZoneId.systemDefault());
        long date = zonedDateTime.toInstant().toEpochMilli();
        var noteInsertQuery = String.format(INSERT_NEW_NOTE_QUERY_PATTERN, noteId, body, personId);

        doInConnection(connection -> {
            try {
                PreparedStatement newPerson = connection.prepareStatement(personInsertQuery);
                newPerson.setObject(1, new Timestamp(birthdayLong));
                newPerson.execute();

                PreparedStatement newNote = connection.prepareStatement(noteInsertQuery);
                newNote.setTimestamp(1, new Timestamp(date));
                newNote.execute();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        });

        Assertions.assertThrows(JdbcDaoException.class, () -> entityManager.find(Person.class, 10L));
        Assertions.assertDoesNotThrow(() -> entityManager.find(Person.class, personId));
        var selectedPerson = entityManager.find(Person.class, personId);
        assertEquals(firstName, selectedPerson.getFirstName());
        assertEquals(lastName, selectedPerson.getLastName());
        assertEquals(birthday.toLocalDate(), selectedPerson.getBirthday());

        Assertions.assertThrows(JdbcDaoException.class, () -> entityManager.find(NoteComplex.class, 10L));
        Assertions.assertDoesNotThrow(() -> entityManager.find(NoteComplex.class, noteId));
        var selectedNote = entityManager.find(NoteComplex.class, noteId);
        assertEquals(body, selectedNote.getBody());
        assertEquals(personId, selectedNote.getPerson().getId());
        assertEquals(createdAt, selectedNote.getCreatedAt());

        Assertions.assertDoesNotThrow(() -> entityManager.remove(selectedNote));
        Assertions.assertThrows(JdbcDaoException.class, () -> entityManager.find(NoteComplex.class, noteId));

        Assertions.assertDoesNotThrow(() -> entityManager.remove(selectedPerson));
        Assertions.assertThrows(JdbcDaoException.class, () -> entityManager.find(Person.class, personId));

        entityManager.close();
    }

    private void doInConnection(Consumer<Connection> consumer) {
        try (Connection connection = dataSource.getConnection()) {
            consumer.accept(connection);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }


}
