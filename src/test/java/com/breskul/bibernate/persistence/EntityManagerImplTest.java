package com.breskul.bibernate.persistence;

import com.breskul.bibernate.AbstractDataSourceTest;
import com.breskul.bibernate.exception.EntityManagerException;
import com.breskul.bibernate.exception.TransactionException;
import com.breskul.bibernate.persistence.testmodel.*;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import org.junit.jupiter.api.*;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.Month;

import static org.junit.jupiter.api.Assertions.*;

public class EntityManagerImplTest extends AbstractDataSourceTest {

    private EntityManager entityManager;

    @BeforeEach
    void setUp() {
        entityManager = new EntityManagerImpl(dataSource);
    }

    @AfterEach
    void destroy() {
        doInConnection(connection -> {
            try {
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

}
