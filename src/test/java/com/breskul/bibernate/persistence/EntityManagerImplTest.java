package com.breskul.bibernate.persistence;

import com.breskul.bibernate.AbstractDataSourceTest;
import com.breskul.bibernate.exception.EntityManagerException;
import com.breskul.bibernate.exception.InternalException;
import com.breskul.bibernate.exception.JdbcDaoException;
import com.breskul.bibernate.exception.LazyInitializationException;
import com.breskul.bibernate.exception.TransactionException;
import com.breskul.bibernate.persistence.test_model.*;
import com.breskul.bibernate.persistence.test_model.cascadepersist.CompanyCascadePersist;
import com.breskul.bibernate.persistence.test_model.cascadepersist.NoteComplexCascadePersist;
import com.breskul.bibernate.persistence.test_model.cascadepersist.PersonCascadePersist;
import com.breskul.bibernate.persistence.test_model.cascadepersist.PersonProfileCascadePersist;
import com.breskul.bibernate.persistence.util.DaoUtils;
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
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class EntityManagerImplTest extends AbstractDataSourceTest {
    public static final String FIRST_NAME = "Serhii";
    public static final String LAST_NAME = "Yevtushok";
    public static final LocalDate BIRTHDAY = LocalDate.of(2023, Month.JANUARY, 1);
    public static final String NOTE_BODY = "WOW, my brain is steaming!";
    public static final String TABLE_NOT_FOUND_MESSAGE = "entity is not marked with @Table annotation - mark entity with table annotation";
    public static final String NO_ENTITY_MESSAGE = "com.breskul.bibernate.persistence.test_model.PersonWithoutEntity is not a valid entity class - @Entity annotation should be present";
    public static final String ID_AND_STRATEGY_MESSAGE = "Detached entity is passed to persist - Make sure that you don't set id manually when using @GeneratedValue";

    private EntityManager entityManager;

    @BeforeEach
    void setUp() {
        entityManager = new EntityManagerImpl(dataSource);
    }

    @AfterEach
    void destroy() {
        doInConnection(connection -> {
            try {
                PreparedStatement profiles = connection.prepareStatement(CLEAN_PROFILES_TABLE);
                profiles.execute();

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
        this.entityManager.close();
    }

    @Test
    @DisplayName("Find entity with not default constructor")
    public void getEntityWithNotDefaultConstructor() {
        entityManager.getTransaction().begin();
        assertThrows(InternalException.class, () -> entityManager.find(PersonWithoutDefaultConstructor.class, 1L));
        entityManager.getTransaction().rollback();
    }

    @Test
    @DisplayName("Test no table annotation present")
    void testValidateEntityNoTable() {
        PersonWithoutTable person = new PersonWithoutTable();
        person.setFirstName(FIRST_NAME);
        person.setLastName(LAST_NAME);
        person.setBirthday(BIRTHDAY);
        JdbcDaoException jdbcDaoException = assertThrows(JdbcDaoException.class, () -> entityManager.persist(person));
        assertEquals(TABLE_NOT_FOUND_MESSAGE, jdbcDaoException.getMessage());
    }

    @Test
    @DisplayName("Test sequence in database is not found")
    void testValidateEntityNoSequence() {
        PersonSequence person = new PersonSequence();
        person.setFirstName(FIRST_NAME);
        person.setLastName(LAST_NAME);
        person.setBirthday(BIRTHDAY);
        assertThrows(TransactionException.class, () -> entityManager.persist(person));
    }

    @Test
    @DisplayName("Test no entity annotation present")
    void testValidateEntityNoEntity() {
        PersonWithoutEntity person = new PersonWithoutEntity();
        person.setFirstName(FIRST_NAME);
        person.setLastName(LAST_NAME);
        person.setBirthday(BIRTHDAY);
        JdbcDaoException jdbcDaoException = assertThrows(JdbcDaoException.class, () -> entityManager.persist(person));
        assertEquals(NO_ENTITY_MESSAGE, jdbcDaoException.getMessage());
    }

    @Test
    @DisplayName("Test entity already have an id")
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
    @DisplayName("Insert person with note")
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

    @Test
    @DisplayName("Test insert one person with multiple notes")
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
    @DisplayName("Test insert one person with multiple notes and multiple companies")
    public void testInsertPersonWithMultipleNotes() {
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
        validateNote(note1.getId(), person.getId());
        validateNote(note2.getId(), person.getId());

        validateCompany(company1.getId(), note1.getId());
        validateCompany(company2.getId(), note1.getId());

        validateCompany(company3.getId(), note2.getId());
        validateCompany(company4.getId(), note2.getId());
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
    @DisplayName("Test close EntityManager")
    public void testCloseEntityManager() {
        assertTrue(entityManager.isOpen());

        entityManager.close();

        assertFalse(entityManager.isOpen());

        Person person = new Person();
        Assertions.assertThrows(EntityManagerException.class, () -> entityManager.find(Person.class, 1L));
        Assertions.assertThrows(EntityManagerException.class, () -> entityManager.remove(person));
        Assertions.assertThrows(EntityManagerException.class, () -> entityManager.persist(person));
        Assertions.assertThrows(EntityManagerException.class, () -> entityManager.merge(person));
    }

    @Test
    @DisplayName("Test remove method")
    public void testRemoveMethod() {
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


        entityTransaction.begin();
        entityManager.remove(person);
        entityTransaction.commit();

        checkEntityDoesNotExist(person);
        checkEntityDoesNotExist(note1);
        checkEntityDoesNotExist(note2);
        checkEntityDoesNotExist(company1);
        checkEntityDoesNotExist(company2);
        checkEntityDoesNotExist(company3);
        checkEntityDoesNotExist(company4);
    }
    @Test
    @DisplayName("Test remove method throws exception")
    public void testRemoveMethodThrowsException() {
        PersonCascadePersist person = new PersonCascadePersist();
        person.setFirstName(FIRST_NAME);
        person.setLastName(LAST_NAME);
        person.setBirthday(BIRTHDAY);

        NoteComplexCascadePersist note1 = new NoteComplexCascadePersist();
        note1.setBody(NOTE_BODY);

        NoteComplexCascadePersist note2 = new NoteComplexCascadePersist();
        note2.setBody(NOTE_BODY);

        person.addNote(note1);
        person.addNote(note2);

        CompanyCascadePersist company1 = new CompanyCascadePersist();
        company1.setName("company1");

        CompanyCascadePersist company2 = new CompanyCascadePersist();
        company2.setName("company2");

        CompanyCascadePersist company3 = new CompanyCascadePersist();
        company3.setName("company3");

        CompanyCascadePersist company4 = new CompanyCascadePersist();
        company4.setName("company4");

        note1.addCompany(company1);
        note1.addCompany(company2);

        note2.addCompany(company3);
        note2.addCompany(company4);

        EntityTransaction entityTransaction = entityManager.getTransaction();

        entityTransaction.begin();
        entityManager.persist(person);
        entityTransaction.commit();


        entityTransaction.begin();
        assertThrows(JdbcDaoException.class, () -> entityManager.remove(person));
        assertThrows(JdbcDaoException.class, () -> entityManager.remove(note1));
        assertThrows(JdbcDaoException.class, () -> entityManager.remove(note2));
        entityTransaction.commit();

        entityTransaction.begin();
        entityManager.remove(company1);
        entityManager.remove(company2);
        entityManager.remove(company3);
        entityManager.remove(company4);
        entityTransaction.commit();

        checkEntityDoesNotExist(company1);
        checkEntityDoesNotExist(company2);
        checkEntityDoesNotExist(company3);
        checkEntityDoesNotExist(company4);

    }
    @Test
    @DisplayName("Test remove method on complex entity and one to one mapping")
    public void testRemoveMethodOneToOne(){
        PersonCascadePersist person = new PersonCascadePersist();
        person.setFirstName(FIRST_NAME);
        person.setLastName(LAST_NAME);
        person.setBirthday(BIRTHDAY);

        PersonProfileCascadePersist personProfile = new PersonProfileCascadePersist();

        personProfile.setProfile("my profile");
        personProfile.setPersonCascadePersist(person);

        NoteComplexCascadePersist note1 = new NoteComplexCascadePersist();
        note1.setBody(NOTE_BODY);

        NoteComplexCascadePersist note2 = new NoteComplexCascadePersist();
        note2.setBody(NOTE_BODY);

        person.addNote(note1);
        person.addNote(note2);

        CompanyCascadePersist company1 = new CompanyCascadePersist();
        company1.setName("company1");

        CompanyCascadePersist company2 = new CompanyCascadePersist();
        company2.setName("company2");

        CompanyCascadePersist company3 = new CompanyCascadePersist();
        company3.setName("company3");

        CompanyCascadePersist company4 = new CompanyCascadePersist();
        company4.setName("company4");

        note1.addCompany(company1);
        note1.addCompany(company2);

        note2.addCompany(company3);
        note2.addCompany(company4);

        EntityTransaction entityTransaction = entityManager.getTransaction();

        entityTransaction.begin();
        entityManager.persist(person);
        entityManager.persist(personProfile);
        entityTransaction.commit();

        entityTransaction.begin();
        assertThrows(JdbcDaoException.class, () -> entityManager.remove(person));
        entityTransaction.commit();

        entityTransaction.begin();
        entityManager.remove(personProfile);
        entityTransaction.commit();

        checkEntityDoesNotExist(personProfile);
    }
    private void checkEntityDoesNotExist(Object entity) {
        var table = DaoUtils.getClassTableName(entity.getClass());
        var identifierName = DaoUtils.getIdentifierFieldName(entity.getClass());
        var identifierValue = DaoUtils.getIdentifierValue(entity);
        String query = String.format("SELECT * FROM %s where %s = %s", table, identifierName, identifierValue);
        try (Connection connection = dataSource.getConnection()) {
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            ResultSet resultSet = preparedStatement.executeQuery();

            assertFalse(resultSet.next());
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    @DisplayName("Test remove method")
    public void removeMethodWithTransaction() {
        PersonWithoutIdAndStrategy person = new PersonWithoutIdAndStrategy();
        person.setId(10L);
        person.setFirstName("firstName");
        person.setLastName("lastName");

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
        long personId = 20L;
        PersonWithoutIdAndStrategy person = new PersonWithoutIdAndStrategy();
        person.setId(personId);
        person.setFirstName("Tom");
        person.setLastName("Hanks");
        var birthday = LocalDateTime.of(1956, Month.JULY, 9, 10, 0, 0).toLocalDate();
        person.setBirthday(birthday);

        EntityTransaction entityTransaction = entityManager.getTransaction();
        entityTransaction.begin();

        assertNull(entityManager.find(PersonWithoutIdAndStrategy.class, personId));
        entityManager.persist(person);
        assertNull(entityManager.find(Person.class, 10L));
        assertDoesNotThrow(() -> entityManager.find(Person.class, personId));
        var selectedPerson = entityManager.find(Person.class, personId);
        assertEquals(person.getFirstName(), selectedPerson.getFirstName());
        assertEquals(person.getLastName(), selectedPerson.getLastName());
        assertEquals(person.getBirthday(), selectedPerson.getBirthday());
        assertDoesNotThrow(() -> entityManager.remove(selectedPerson));
        assertNull(entityManager.find(Person.class, personId));

        entityTransaction.commit();
    }

    @Test
    @DisplayName("Test find method for Entity with ManyToOne relation")
    public void testFindMethodWithManyToOneRelation() {
        EntityTransaction entityTransaction = entityManager.getTransaction();
        entityTransaction.begin();

        assertNull(entityManager.find(NoteWithoutGeneratedValue.class, 30L));

        PersonWithoutGeneratedValue person = new PersonWithoutGeneratedValue();
        person.setFirstName("Keanu");
        person.setLastName("Reeves");
        person.setId(30L);
        person.setBirthday(LocalDateTime.of(1964, Month.SEPTEMBER, 2, 10, 0, 0).toLocalDate());

        entityManager.persist(person);

        NoteWithoutGeneratedValue note = new NoteWithoutGeneratedValue();
        note.setId(30L);
        note.setBody("note");
        note.setPerson(person);

        entityManager.persist(note);

        assertNull(entityManager.find(PersonWithoutGeneratedValue.class, 10L));
        assertDoesNotThrow(() -> entityManager.find(PersonWithoutGeneratedValue.class, person.getId()));
        var selectedPerson = entityManager.find(PersonWithoutGeneratedValue.class, person.getId());
        assertEquals(person.getFirstName(), selectedPerson.getFirstName());
        assertEquals(person.getLastName(), selectedPerson.getLastName());
        assertEquals(person.getBirthday(), selectedPerson.getBirthday());

        assertNull(entityManager.find(NoteWithoutGeneratedValue.class, 10L));
        assertDoesNotThrow(() -> entityManager.find(NoteWithoutGeneratedValue.class, note.getId()));
        var selectedNote = entityManager.find(NoteWithoutGeneratedValue.class, note.getId());
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
        assertNull(entityManager.find(NoteWithoutGeneratedValue.class, 33L));

        NoteWithoutGeneratedValue note = new NoteWithoutGeneratedValue();
        note.setId(33L);
        note.setBody("a new note");

        entityManager.persist(note);

        assertNull(entityManager.find(NoteWithoutGeneratedValue.class, 10L));
        assertDoesNotThrow(() -> entityManager.find(NoteWithoutGeneratedValue.class, note.getId()));
        var selectedNote = entityManager.find(NoteWithoutGeneratedValue.class, note.getId());
        assertNotNull(selectedNote);
        assertEquals(note.getBody(), selectedNote.getBody());
        assertNull(selectedNote.getPerson());

        assertDoesNotThrow(() -> entityManager.remove(selectedNote));
        assertNull(entityManager.find(NoteComplex.class, note.getId()));

        entityManager.getTransaction().commit();
    }

    @Test
    @DisplayName("Test lazy loading. Get Entity list of related objects out of transaction")
    public void testLazyLoadingRelatedObjectsOutOfTransaction() {

        PersonWithoutGeneratedValue person = new PersonWithoutGeneratedValue();
        person.setFirstName("Quentin");
        person.setLastName("Tarantino");
        person.setId(40L);
        person.setBirthday(LocalDateTime.of(1963, Month.MARCH, 27, 10, 0, 0).toLocalDate());

        NoteWithoutGeneratedValue firstNote = new NoteWithoutGeneratedValue();
        firstNote.setId(51L);
        firstNote.setBody("Pulp Fiction. 1994");
        firstNote.setPerson(person);

        NoteWithoutGeneratedValue secondNote = new NoteWithoutGeneratedValue();
        secondNote.setId(52L);
        secondNote.setBody("Once Upon a Time in Hollywood. 2019");
        secondNote.setPerson(person);

        person.addNote(firstNote);
        person.addNote(secondNote);

        entityManager.getTransaction().begin();
        entityManager.persist(person);
        entityManager.getTransaction().commit();

        EntityManagerImpl otherEntityManager = new EntityManagerImpl(dataSource);
        otherEntityManager.getTransaction().begin();
        var selectedPerson = otherEntityManager.find(PersonWithoutGeneratedValue.class, person.getId());
        otherEntityManager.getTransaction().commit();
        otherEntityManager.close();

        assertEquals(person.getFirstName(), selectedPerson.getFirstName());
        assertEquals(person.getLastName(), selectedPerson.getLastName());
        assertEquals(person.getBirthday(), selectedPerson.getBirthday());
        assertThrows(LazyInitializationException.class, () -> selectedPerson.getNotes().size());

        entityManager.getTransaction().begin();
        var selectedPersonWithNotes = entityManager.find(PersonWithoutGeneratedValue.class, person.getId());
        entityManager.getTransaction().commit();
        assertDoesNotThrow(() -> selectedPersonWithNotes.getNotes().size());
    }

    @Test
    @DisplayName("Test lazy loading. Get Entity and list of related objects in bound of transaction")
    public void testLazyLoading() {

        PersonWithoutGeneratedValue person = new PersonWithoutGeneratedValue();
        person.setFirstName("Quentin");
        person.setLastName("Tarantino");
        person.setId(41L);
        person.setBirthday(LocalDateTime.of(1963, Month.MARCH, 27, 10, 0, 0).toLocalDate());

        NoteWithoutGeneratedValue firstNote = new NoteWithoutGeneratedValue();
        firstNote.setId(53L);
        firstNote.setBody("Pulp Fiction. 1994");
        firstNote.setPerson(person);

        NoteWithoutGeneratedValue secondNote = new NoteWithoutGeneratedValue();
        secondNote.setId(54L);
        secondNote.setBody("Once Upon a Time in Hollywood. 2019");
        secondNote.setPerson(person);

        person.addNote(firstNote);
        person.addNote(secondNote);

        entityManager.getTransaction().begin();
        entityManager.persist(person);
        entityManager.getTransaction().commit();

        EntityManagerImpl otherEntityManager = new EntityManagerImpl(dataSource);
        otherEntityManager.getTransaction().begin();
        var selectedPerson = otherEntityManager.find(PersonWithoutGeneratedValue.class, person.getId());
        assertDoesNotThrow(() -> selectedPerson.getNotes().size());
        otherEntityManager.getTransaction().commit();
        otherEntityManager.close();

        assertEquals(person.getFirstName(), selectedPerson.getFirstName());
        assertEquals(person.getLastName(), selectedPerson.getLastName());
        assertEquals(person.getBirthday(), selectedPerson.getBirthday());
        assertEquals(2, selectedPerson.getNotes().size());
    }

    @Test
    @DisplayName("Test eager loading. Get list of related objects out of transaction")
    public void testEagerLoading() {

        var person = new PersonWithoutGeneratedValueWithEagerFetch();
        person.setFirstName("Tom");
        person.setLastName("Cruise");
        person.setId(50L);
        person.setBirthday(LocalDateTime.of(1962, Month.JULY, 3, 5, 0, 0).toLocalDate());

        var firstNote = new NoteWithoutGeneratedValueWithEagerFetchFromPerson();
        firstNote.setId(61L);
        firstNote.setBody("Top Gun: Maverick. 1986");
        firstNote.setPerson(person);

        var secondNote = new NoteWithoutGeneratedValueWithEagerFetchFromPerson();
        secondNote.setId(62L);
        secondNote.setBody("Top Gun: Maverick. 2022");
        secondNote.setPerson(person);

        person.addNote(firstNote);
        person.addNote(secondNote);

        entityManager.getTransaction().begin();
        entityManager.persist(person);
        entityManager.getTransaction().commit();

        entityManager.clear();

        entityManager.getTransaction().begin();
        var selectedPerson = entityManager.find(PersonWithoutGeneratedValueWithEagerFetch.class, person.getId());
        entityManager.getTransaction().commit();

        assertEquals(person.getFirstName(), selectedPerson.getFirstName());
        assertEquals(person.getLastName(), selectedPerson.getLastName());
        assertEquals(person.getBirthday(), selectedPerson.getBirthday());
        assertDoesNotThrow(() -> selectedPerson.getNotes().size());
        assertEquals(2, selectedPerson.getNotes().size());
    }

    @Test
    @DisplayName("Test dirty checking Flush")
    public void testDirtyCheckingFlush() {
        var person = new PersonWithoutGeneratedValueWithEagerFetch();
        person.setFirstName("Tom");
        person.setLastName("Cruise");
        person.setId(50L);
        person.setBirthday(LocalDateTime.of(1962, Month.JULY, 3, 5, 0, 0).toLocalDate());

        var node = new NoteWithoutGeneratedValueWithEagerFetchFromPerson();
        node.setId(61L);
        node.setBody("Top Gun: Maverick. 1986");
        node.setPerson(person);

        person.addNote(node);

        entityManager.getTransaction().begin();

        entityManager.persist(person);

        person.setFirstName("newFirstName");
        node.setBody("newBody");

        entityManager.flush();
        entityManager.clear();

        var selectedPerson = entityManager.find(PersonWithoutGeneratedValueWithEagerFetch.class, person.getId());
        var selectedNode = entityManager.find(NoteWithoutGeneratedValueWithEagerFetchFromPerson.class, node.getId());
        entityManager.getTransaction().commit();
        assertEquals(selectedPerson.getFirstName(), person.getFirstName());
        assertEquals(selectedNode.getBody(), node.getBody());
    }

    @Test
    @DisplayName("Test dirty checking Commit")
    public void testDirtyCheckingCommit() {
        var person = new PersonWithoutGeneratedValueWithEagerFetch();
        person.setFirstName("Tom");
        person.setLastName("Cruise");
        person.setId(50L);
        person.setBirthday(LocalDateTime.of(1962, Month.JULY, 3, 5, 0, 0).toLocalDate());

        var node = new NoteWithoutGeneratedValueWithEagerFetchFromPerson();
        node.setId(61L);
        node.setBody("Top Gun: Maverick. 1986");
        node.setPerson(person);

        person.addNote(node);

        entityManager.getTransaction().begin();
        entityManager.persist(person);
        person.setFirstName("newFirstName");
        node.setBody("newBody");
        entityManager.getTransaction().commit();
        entityManager.clear();

        entityManager.getTransaction().begin();
        var selectedPerson = entityManager.find(PersonWithoutGeneratedValueWithEagerFetch.class, person.getId());
        var selectedNode = entityManager.find(NoteWithoutGeneratedValueWithEagerFetchFromPerson.class, node.getId());
        entityManager.getTransaction().commit();
        assertEquals(selectedPerson.getFirstName(), person.getFirstName());
        assertEquals(selectedNode.getBody(), node.getBody());
    }

    @Test
    @DisplayName("Test dirty checking for lazy initialization")
    public void testDirtyCheckingForLazyList() {
        PersonWithoutGeneratedValue person = new PersonWithoutGeneratedValue();
        person.setFirstName("Quentin");
        person.setLastName("Tarantino");
        person.setId(41L);
        person.setBirthday(LocalDateTime.of(1963, Month.MARCH, 27, 10, 0, 0).toLocalDate());

        NoteWithoutGeneratedValue node = new NoteWithoutGeneratedValue();
        node.setId(53L);
        node.setBody("Pulp Fiction. 1994");
        node.setPerson(person);

        person.addNote(node);

        entityManager.getTransaction().begin();
        entityManager.persist(person);
        entityManager.getTransaction().commit();

        entityManager.clear();

        entityManager.getTransaction().begin();
        var selectedPerson = entityManager.find(PersonWithoutGeneratedValue.class, person.getId());
        List<NoteWithoutGeneratedValue> list = selectedPerson.getNotes();
        NoteWithoutGeneratedValue lazyNode = list.get(0);
        lazyNode.setBody("New test body");
        entityManager.getTransaction().commit();

        entityManager.getTransaction().begin();
        var selectedNode = entityManager.find(NoteWithoutGeneratedValue.class, lazyNode.getId());
        entityManager.getTransaction().commit();
        assertEquals(lazyNode.getBody(), selectedNode.getBody());
    }

    @Test
    @DisplayName("Test dirty checking for cascade type persist")
    public void testDirtyCheckingForCascadePersist() {
        PersonCascadePersist person = new PersonCascadePersist();
        person.setFirstName("Quentin");
        person.setLastName("Tarantino");
        person.setBirthday(LocalDateTime.of(1963, Month.MARCH, 27, 10, 0, 0).toLocalDate());

        entityManager.getTransaction().begin();

        entityManager.persist(person);
        NoteComplexCascadePersist node = new NoteComplexCascadePersist();
        node.setBody("Pulp Fiction. 1994");
        node.setPerson(person);
        person.addNote(node);

        entityManager.getTransaction().commit();

        entityManager.clear();

        entityManager.getTransaction().begin();
        var selectedNode = entityManager.find(NoteComplexCascadePersist.class, node.getId());
        entityManager.getTransaction().commit();
        assertNotNull(selectedNode);
    }

    @Test
    @DisplayName("Test dirty checking for complex entities")
    public void testDirtyCheckingForComplexEntity(){
        PersonCascadePersist person = new PersonCascadePersist();
        person.setFirstName(FIRST_NAME);
        person.setLastName(LAST_NAME);
        person.setBirthday(BIRTHDAY);

        NoteComplexCascadePersist note1 = new NoteComplexCascadePersist();
        note1.setBody(NOTE_BODY);

        NoteComplexCascadePersist note2 = new NoteComplexCascadePersist();
        note2.setBody(NOTE_BODY);

        person.addNote(note1);
        person.addNote(note2);

        CompanyCascadePersist company1 = new CompanyCascadePersist();
        company1.setName("company1");

        CompanyCascadePersist company2 = new CompanyCascadePersist();
        company2.setName("company2");

        CompanyCascadePersist company3 = new CompanyCascadePersist();
        company3.setName("company3");

        CompanyCascadePersist company4 = new CompanyCascadePersist();
        company4.setName("company4");

        note1.addCompany(company1);
        note1.addCompany(company2);

        note2.addCompany(company3);
        note2.addCompany(company4);

        EntityTransaction entityTransaction = entityManager.getTransaction();

        entityTransaction.begin();
        entityManager.persist(person);
        entityTransaction.commit();

        entityTransaction.begin();
        var managedPerson = entityManager.find(PersonCascadePersist.class, person.getId());
        var notes = managedPerson.getNotes();
        var managedNote1 = notes.get(0);
        var managedNote2 = notes.get(1);
        var listCompanies2 = managedNote2.getCompanies();

        for (var company: listCompanies2){
            managedNote1.addCompany(company);
        }
        entityTransaction.commit();

        validateCompany2(company1.getId(), managedNote1.getId());
        validateCompany2(company2.getId(), managedNote1.getId());
        validateCompany2(company3.getId(), managedNote1.getId());
        validateCompany2(company4.getId(), managedNote1.getId());
    }

    private void validateCompany2(Long id, Long parentId) {
        try (Connection connection = dataSource.getConnection()) {
            PreparedStatement preparedStatement = connection.prepareStatement(String.format("SELECT * FROM companies where id = %d and note_id = %d", id, parentId));
            System.out.println("SQL:" + preparedStatement);
            ResultSet resultSet = preparedStatement.executeQuery();

            assertTrue(resultSet.next());
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    @DisplayName("Test merge method")
    public void testMerge() {

        Person person = new Person();
        person.setFirstName("Harry");
        person.setLastName("Potter");
        person.setBirthday(LocalDate.of(1980, Month.JULY, 31));

        NoteComplex note1 = new NoteComplex();
        note1.setBody("My name is Harry Potter");
        person.addNote(note1);

        NoteComplex note2 = new NoteComplex();
        note2.setBody("Do you know anything about the Camber of Secret?");
        person.addNote(note2);

        entityManager.getTransaction().begin();
        entityManager.persist(person);
        entityManager.getTransaction().commit();

        entityManager.clear();

        assertFalse(entityManager.contains(person));

        person.setFirstName("Ron");
        person.getNotes().get(0).setBody("My name is Ron");

        entityManager.getTransaction().begin();
        Person managedPerson = entityManager.merge(person);

        assertEquals("Ron", managedPerson.getFirstName());
        assertEquals("My name is Ron", managedPerson.getNotes().get(0).getBody());
        assertNotSame(person, managedPerson);
        assertTrue(entityManager.contains(managedPerson));
        entityManager.getTransaction().commit();

        entityManager.getTransaction().begin();
        Person foundPerson = entityManager.find(Person.class, person.getId());
        assertEquals("Ron", foundPerson.getFirstName());
        assertEquals("My name is Ron", foundPerson.getNotes().get(0).getBody());
        entityManager.getTransaction().commit();

    }

    @Test
    @DisplayName("Test merge method with CascadeType.PERSIST")
    public void testMergeCascadePersist() {

        PersonCascadePersist person = new PersonCascadePersist();
        person.setFirstName("Harry");
        person.setLastName("Potter");
        person.setBirthday(LocalDate.of(1980, Month.JULY, 31));

        NoteComplexCascadePersist note1 = new NoteComplexCascadePersist();
        note1.setBody("My name is Harry Potter");
        person.addNote(note1);

        NoteComplexCascadePersist note2 = new NoteComplexCascadePersist();
        note2.setBody("Do you know anything about the Camber of Secret?");
        person.addNote(note2);

        entityManager.getTransaction().begin();
        entityManager.persist(person);
        entityManager.getTransaction().commit();

        entityManager.clear();

        assertFalse(entityManager.contains(person));

        person.setFirstName("Ron");
        person.getNotes().get(0).setBody("My name is Ron");

        entityManager.getTransaction().begin();
        PersonCascadePersist managedPerson = entityManager.merge(person);

        assertEquals("Ron", managedPerson.getFirstName());
        assertEquals("My name is Harry Potter", managedPerson.getNotes().get(0).getBody());
        assertNotSame(person, managedPerson);
        assertTrue(entityManager.contains(managedPerson));
        entityManager.getTransaction().commit();

        entityManager.getTransaction().begin();
        PersonCascadePersist foundPerson = entityManager.find(PersonCascadePersist.class, person.getId());
        assertEquals("Ron", foundPerson.getFirstName());
        assertEquals("My name is Harry Potter", foundPerson.getNotes().get(0).getBody());
        entityManager.getTransaction().commit();

    }

    @Test
    @DisplayName("Merge Transient entity.")
    public void testMergeTransientEntity() {
        Person person = new Person();
        person.setFirstName("Harry");
        person.setLastName("Potter");
        person.setBirthday(LocalDate.of(1980, Month.JULY, 31));

        NoteComplex note1 = new NoteComplex();
        note1.setBody("My name is Harry Potter");
        person.addNote(note1);

        NoteComplex note2 = new NoteComplex();
        note2.setBody("Do you know anything about the Camber of Secret?");
        person.addNote(note2);

        entityManager.getTransaction().begin();
        Person managedPerson = entityManager.merge(person);
        entityManager.getTransaction().commit();

        assertEquals("Harry", managedPerson.getFirstName());
        assertEquals("My name is Harry Potter", managedPerson.getNotes().get(0).getBody());
        assertTrue(entityManager.contains(managedPerson));
        assertNotSame(person, managedPerson);
    }

    @Test
    @DisplayName("Test contains method.")
    public void testContains() {
        Person person = new Person();
        person.setFirstName("Harry");
        person.setLastName("Potter");
        person.setBirthday(LocalDate.of(1980, Month.JULY, 31));

        NoteComplex note1 = new NoteComplex();
        note1.setBody("My name is Harry Potter");
        person.addNote(note1);

        NoteComplex note2 = new NoteComplex();
        note2.setBody("Do you know anything about the Camber of Secret?");
        person.addNote(note2);

        entityManager.getTransaction().begin();
        entityManager.persist(person);
        entityManager.getTransaction().commit();

        assertTrue(entityManager.contains(person));
        assertTrue(entityManager.contains(note1));
        assertTrue(entityManager.contains(note2));

        entityManager.clear();

        assertFalse(entityManager.contains(person));
        assertFalse(entityManager.contains(note1));
        assertFalse(entityManager.contains(note2));
    }

    @Test
    @DisplayName("Test detach method.")
    public void testDetach() {
        Person person = new Person();
        person.setFirstName("Harry");
        person.setLastName("Potter");
        person.setBirthday(LocalDate.of(1980, Month.JULY, 31));

        NoteComplex note1 = new NoteComplex();
        note1.setBody("My name is Harry Potter");
        person.addNote(note1);

        NoteComplex note2 = new NoteComplex();
        note2.setBody("Do you know anything about the Camber of Secret?");
        person.addNote(note2);

        entityManager.getTransaction().begin();
        entityManager.persist(person);
        entityManager.getTransaction().commit();

        entityManager.detach(person);

        assertFalse(entityManager.contains(person));
        assertTrue(entityManager.contains(note1));
        assertTrue(entityManager.contains(note2));
    }

}
