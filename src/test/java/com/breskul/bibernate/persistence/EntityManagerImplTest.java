package com.breskul.bibernate.persistence;

import com.breskul.bibernate.AbstractDataSourceTest;
import com.breskul.bibernate.exception.JdbcDaoException;
import com.breskul.bibernate.exception.TransactionException;
import com.breskul.bibernate.persistence.testmodel.*;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import org.junit.jupiter.api.*;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;

import static org.junit.jupiter.api.Assertions.*;

public class EntityManagerImplTest extends AbstractDataSourceTest {
    public static final String FIRST_NAME = "Serhii";
    public static final String LAST_NAME = "Yevtushok";
    public static final LocalDate BIRTHDAY = LocalDate.of(2023, Month.JANUARY, 1);
    public static final String NOTE_BODY = "WOW, my brain is steaming!";
    public static final String TABLE_NOT_FOUND_MESSAGE = "entity is not marked with @Table annotation - mark entity with table annotation";
    public static final String NO_ENTITY_MESSAGE = "com.breskul.bibernate.persistence.testmodel.PersonWithoutEntity is not a valid entity class - @Entity annotation should be present";
    public static final String ID_AND_STRATEGY_MESSAGE = "detached entity is passed to persist - Make sure that you don't set id manually when using @GeneratedValue";
    public static final String WITHOUT_ID_AND_STRATEGY = "annotation GeneratedValue is not found - mark class with the GeneratedValue annotation";

    private EntityManager entityManager;

    @BeforeAll
    void setUp() {
        entityManager = new EntityManagerImpl(dataSource);
    }

    @AfterEach
    void destroy() {
        doInConnection(connection -> {
            try {
                PreparedStatement companies = connection.prepareStatement(CLEAN_COMPANY_TABLE);
                companies.execute();
                PreparedStatement notes = connection.prepareStatement(CLEAN_NOTE_TABLE);
                notes.execute();

                PreparedStatement persons = connection.prepareStatement(CLEAN_PERSON_TABLE);
                persons.execute();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        });
        entityManager.close();
    }
    @Test
    @Order(1)
    @DisplayName("1. Test no table annotation present")
    void testValidateEntityNoTable() {
        PersonWithoutTable person = new PersonWithoutTable();
        person.setFirstName(FIRST_NAME);
        person.setLastName(LAST_NAME);
        person.setBirthday(BIRTHDAY);
        JdbcDaoException jdbcDaoException = assertThrows(JdbcDaoException.class, () -> entityManager.persist(person));
        assertEquals(TABLE_NOT_FOUND_MESSAGE, jdbcDaoException.getMessage());
    }

    @Test
    @DisplayName("2. Test sequence in database is not found")
    @Order(2)
    void testValidateEntityNoSequence() {
        PersonSequence person = new PersonSequence();
        person.setFirstName(FIRST_NAME);
        person.setLastName(LAST_NAME);
        person.setBirthday(BIRTHDAY);
        assertThrows(TransactionException.class, () -> entityManager.persist(person));
    }

    @Test
    @DisplayName("3. Test no entity annotation present")
    @Order(3)
    void testValidateEntityNoEntity() {
        PersonWithoutEntity person = new PersonWithoutEntity();
        person.setFirstName(FIRST_NAME);
        person.setLastName(LAST_NAME);
        person.setBirthday(BIRTHDAY);
        JdbcDaoException jdbcDaoException = assertThrows(JdbcDaoException.class, () -> entityManager.persist(person));
        assertEquals(NO_ENTITY_MESSAGE, jdbcDaoException.getMessage());
    }

    @Test
    @DisplayName("4. Test entity already have an id")
    @Order(4)
    void testValidateEntityIdAlreadyPresent() {
        PersonWithIdAndStrategy person = new PersonWithIdAndStrategy();
        person.setId(1L);
        person.setFirstName(FIRST_NAME);
        person.setLastName(LAST_NAME);
        person.setBirthday(BIRTHDAY);
        JdbcDaoException jdbcDaoException = assertThrows(JdbcDaoException.class, () -> entityManager.persist(person));
        assertEquals(ID_AND_STRATEGY_MESSAGE, jdbcDaoException.getMessage());
    }

    @Test
    @DisplayName("5. Test entity does not have GeneratedValue")
    @Order(5)
    void testValidateEntityGeneratedValueIsNotPresent() {
        PersonWithoutIdAndStrategy person = new PersonWithoutIdAndStrategy();
        person.setFirstName(FIRST_NAME);
        person.setLastName(LAST_NAME);
        person.setBirthday(BIRTHDAY);
        JdbcDaoException jdbcDaoException = assertThrows(JdbcDaoException.class, () -> entityManager.persist(person));
        assertEquals(WITHOUT_ID_AND_STRATEGY, jdbcDaoException.getMessage());
    }

    @Test
    @DisplayName("6. Insert person with note")
    @Order(6)
    void insertPersonWithNotes() {

        NoteComplex note = new NoteComplex();
        note.setBody(NOTE_BODY);

        Person person = new Person();
        person.setFirstName(FIRST_NAME);
        person.setLastName(LAST_NAME);
        person.addNote(note);
        person.setBirthday(BIRTHDAY);
        EntityTransaction entityTransaction = entityManager.getTransaction();
        entityTransaction.begin();
        entityManager.persist(person);
        entityTransaction.commit();

        validatePerson(person.getId());
        validateNote(note.getId(), person.getId());
    }
    @Test
    @DisplayName("7. Test insert one person with multiple notes")
    @Order(7)
    void testInsertOnlyPerson() {
        Person person = new Person();
        person.setFirstName(FIRST_NAME);
        person.setLastName(LAST_NAME);
        person.setBirthday(BIRTHDAY);
        NoteComplex note1 = new NoteComplex();
        note1.setBody(NOTE_BODY);

        NoteComplex note2 = new NoteComplex();
        note2.setBody(NOTE_BODY);

        NoteComplex note3 = new NoteComplex();
        note3.setBody(NOTE_BODY);

        person.addNote(note1);
        person.addNote(note2);
        person.addNote(note3);
        EntityTransaction entityTransaction = entityManager.getTransaction();
        entityTransaction.begin();
        entityManager.persist(person);
        entityTransaction.commit();

        validatePerson(person.getId());
        validateNote(note1.getId(), person.getId());
        validateNote(note2.getId(), person.getId());
        validateNote(note3.getId(), person.getId());
    }
    @Test
    @DisplayName("8. Test insert one person with multiple notes and multiple companies")
    @Order(8)
    public void testInsertPersonWithMultipleNotes(){
        Person person = new Person();
        person.setFirstName(FIRST_NAME);
        person.setLastName(LAST_NAME);
        person.setBirthday(BIRTHDAY);

        NoteComplex note1 = new NoteComplex();
        note1.setBody(NOTE_BODY);

        NoteComplex note2 = new NoteComplex();
        note2.setBody(NOTE_BODY);

        person.addNote(note1);
        person.addNote(note2);

        Company company1 = new Company();
        company1.setName("company1");

        Company company2 = new Company();
        company2.setName("company2");

        Company company3 = new Company();
        company3.setName("company3");

        Company company4 = new Company();
        company4.setName("company4");

        note1.addCompany(company1);
        note1.addCompany(company2);

        note2.addCompany(company3);
        note2.addCompany(company4);

        EntityTransaction entityTransaction = entityManager.getTransaction();

        entityTransaction.begin();
        entityManager.persist(person);
        entityTransaction.commit();

        validatePerson(person.getId());
        validateNote(note1.getId(),person.getId());
        validateNote(note2.getId(),person.getId());

        validateCompany(company1.getId(), note1.getId());
        validateCompany(company2.getId(), note1.getId());

        validateCompany(company3.getId(), note2.getId());
        validateCompany(company4.getId(), note2.getId());
    }


    private void validatePerson(Long id) {
        try (Connection connection = dataSource.getConnection()) {
            PreparedStatement preparedStatement = connection.prepareStatement(String.format("SELECT * FROM users where id = %d", id));
            ResultSet resultSet = preparedStatement.executeQuery();

            assertTrue(resultSet.next());
            assertEquals(id, resultSet.getLong(1));
            assertEquals(FIRST_NAME, resultSet.getString(2));
            assertEquals(LAST_NAME, resultSet.getString(3));
            assertEquals(BIRTHDAY, resultSet.getDate(4).toLocalDate());
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private void validateNote(Long id, Long parentId) {
        try (Connection connection = dataSource.getConnection()) {
            PreparedStatement preparedStatement = connection.prepareStatement(String.format("SELECT * FROM notes where id = %d", id));
            ResultSet resultSet = preparedStatement.executeQuery();

            assertTrue(resultSet.next());
            assertEquals(id, resultSet.getLong(1));
            assertEquals(NOTE_BODY, resultSet.getString(2));
            assertEquals(parentId, resultSet.getLong(4));
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private void validateCompany(Long id, Long parentId) {
        try (Connection connection = dataSource.getConnection()) {
            PreparedStatement preparedStatement = connection.prepareStatement(String.format("SELECT * FROM companies where id = %d", id));
            ResultSet resultSet = preparedStatement.executeQuery();

            assertTrue(resultSet.next());
            assertEquals(id, resultSet.getLong(1));
            assertEquals(parentId, resultSet.getLong(3));
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    @DisplayName("Test remove method without transaction")
    public void removeMethodWithoutTransaction() {
        EntityManager entityManager = new EntityManagerImpl(dataSource);
        Person person = new Person();
        person.setId(10L);
        person.setFirstName("user");

        Assertions.assertThrows(TransactionException.class, () -> entityManager.remove(person));
    }

    @Test
    @DisplayName("Test remove method")
    public void removeMethodWithTransaction() {
        Person person = new Person();
        person.setFirstName("firstName");
        person.setLastName("lastName");
        person.setBirthday(BIRTHDAY);

        EntityTransaction entityTransaction = entityManager.getTransaction();
        entityTransaction.begin();
        entityManager.persist(person);
        entityTransaction.commit();

        entityTransaction.begin();
        assertDoesNotThrow(() -> entityManager.remove(person));
        entityTransaction.commit();
    }

    @Test
    @DisplayName("Test find method for Entity with OneToMany relation")
    public void testFindMethodWithOneToManyRelation() {
        Person person = new Person();
        person.setFirstName("Tom");
        person.setLastName("Hanks");
        var birthday = LocalDateTime.of(1956, Month.JULY, 9, 10, 0, 0).toLocalDate();
        person.setBirthday(birthday);

        EntityTransaction entityTransaction = entityManager.getTransaction();
        entityTransaction.begin();

        assertNull(entityManager.find(PersonWithoutIdAndStrategy.class, person.getId()));
        entityManager.persist(person);
        assertNull(entityManager.find(Person.class, 10L));
        assertDoesNotThrow(() -> entityManager.find(Person.class, person.getId()));
        var selectedPerson = entityManager.find(Person.class, person.getId());
        assertEquals(person.getFirstName(), selectedPerson.getFirstName());
        assertEquals(person.getLastName(), selectedPerson.getLastName());
        assertEquals(person.getBirthday(), selectedPerson.getBirthday());
        assertDoesNotThrow(() -> entityManager.remove(selectedPerson));
        assertNull(entityManager.find(Person.class, person.getId()));

        entityTransaction.commit();
    }

    @Test
    @DisplayName("Test find method for Entity with ManyToOne relation")
    public void testFindMethodWithManyToOneRelation() {
        EntityTransaction entityTransaction = entityManager.getTransaction();
        entityTransaction.begin();

        assertNull(entityManager.find(NoteComplex.class, 30L));

        Person person = new Person();
        person.setFirstName("Keanu");
        person.setLastName("Reeves");
        person.setBirthday(LocalDateTime.of(1964, Month.SEPTEMBER, 2, 10, 0, 0).toLocalDate());

        entityManager.persist(person);

        NoteComplex note = new NoteComplex();
        note.setBody("note");
        note.setPerson(person);

        entityManager.persist(note);

        assertNotNull(entityManager.find(Person.class, person.getId()));
        var selectedPerson = entityManager.find(Person.class, person.getId());
        assertEquals(person.getFirstName(), selectedPerson.getFirstName());
        assertEquals(person.getLastName(), selectedPerson.getLastName());
        assertEquals(person.getBirthday(), selectedPerson.getBirthday());

        assertNotNull(entityManager.find(NoteComplex.class, note.getId()));
        var selectedNote = entityManager.find(NoteComplex.class, note.getId());
        assertEquals(note.getBody(), selectedNote.getBody());
        assertEquals(note.getPerson().getId(), selectedNote.getPerson().getId());

        assertDoesNotThrow(() -> entityManager.remove(selectedNote));
        assertNull(entityManager.find(NoteComplex.class, note.getId()));

        assertDoesNotThrow(() -> entityManager.remove(selectedPerson));
        assertNull(entityManager.find(Person.class, person.getId()));

        entityTransaction.commit();
    }

    @Test
    @DisplayName("Test find method for Entity with ManyToOne relation. Related Object is Null")
    public void testFindMethodWithManyToOneRelationRelatedObjectIsNull() {
        entityManager.getTransaction().begin();
        assertNull(entityManager.find(NoteComplex.class, 33L));

        NoteComplex note = new NoteComplex();
        note.setBody(NOTE_BODY);
        note.setCreatedAt(LocalDateTime.now());

        entityManager.persist(note);

        assertNotNull(entityManager.find(NoteComplex.class, note.getId()));

        var selectedNote = entityManager.find(NoteComplex.class, note.getId());
        assertNotNull(selectedNote);
        assertEquals(note.getBody(), selectedNote.getBody());
        assertNull(selectedNote.getPerson());

        assertDoesNotThrow(() -> entityManager.remove(selectedNote));
        assertNull(entityManager.find(NoteComplex.class, note.getId()));

        entityManager.getTransaction().commit();
    }

}
