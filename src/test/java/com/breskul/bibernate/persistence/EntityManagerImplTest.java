package com.breskul.bibernate.persistence;

import com.breskul.bibernate.AbstractDataSourceTest;
import com.breskul.bibernate.exception.JdbcDaoException;
import com.breskul.bibernate.exception.TransactionException;
import com.breskul.bibernate.persistence.testmodel.NodeWithoutGeneratedValue;
import com.breskul.bibernate.persistence.testmodel.NoteComplex;
import com.breskul.bibernate.persistence.testmodel.Person;
import com.breskul.bibernate.persistence.testmodel.PersonWithoutGeneratedValue;
import com.breskul.bibernate.persistence.testmodel.PersonWithoutIdAndStrategy;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import org.junit.jupiter.api.*;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.Month;

import static org.junit.jupiter.api.Assertions.assertEquals;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
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
    @Order(1)
    @DisplayName("1. Test remove method without transaction")
    public void removeMethodWithoutTransaction() {
        EntityManager entityManager = new EntityManagerImpl(dataSource);
        Person person = new Person();
        person.setId(10L);
        person.setFirstName("user");

        Assertions.assertThrows(TransactionException.class, () -> entityManager.remove(person));
    }

    @Test
    @Order(2)
    @DisplayName("1. Test remove method")
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
        Assertions.assertDoesNotThrow(() -> entityManager.remove(person));
        entityTransaction.commit();
    }

    @Test
    @Order(2)
    @DisplayName("2. Test find method for Entity with OneToMany relation")
    public void testFindMethodWithOneToManyRelation() {
        long personId = 20L;
        PersonWithoutIdAndStrategy person = new PersonWithoutIdAndStrategy();
        person.setId(personId);
        person.setFirstName("Tom");
        person.setLastName("Hanks");
        var birthday = LocalDateTime.of(1956, Month.JULY, 9, 10,0,0).toLocalDate();
        person.setBirthday(birthday);

        EntityTransaction entityTransaction = entityManager.getTransaction();
        entityTransaction.begin();

        Assertions.assertThrows(JdbcDaoException.class, () -> entityManager.find(PersonWithoutIdAndStrategy.class, personId));
        entityManager.persist(person);
        Assertions.assertThrows(JdbcDaoException.class, () -> entityManager.find(Person.class, 10L));
        Assertions.assertDoesNotThrow(() -> entityManager.find(Person.class, personId));
        var selectedPerson = entityManager.find(Person.class, personId);
        assertEquals(person.getFirstName(), selectedPerson.getFirstName());
        assertEquals(person.getLastName(), selectedPerson.getLastName());
        assertEquals(person.getBirthday(), selectedPerson.getBirthday());
        Assertions.assertDoesNotThrow(() -> entityManager.remove(selectedPerson));
        Assertions.assertThrows(JdbcDaoException.class, () -> entityManager.find(Person.class, personId));

        entityTransaction.commit();
    }

    @Test
    @Order(3)
    @DisplayName("3. Test find method for Entity with ManyToOne relation")
    public void testFindMethodWithManyToOneRelation() {
        EntityTransaction entityTransaction = entityManager.getTransaction();
        entityTransaction.begin();

        Assertions.assertThrows(JdbcDaoException.class, () -> entityManager.find(NodeWithoutGeneratedValue.class, 30L));

        PersonWithoutGeneratedValue person = new PersonWithoutGeneratedValue();
        person.setFirstName("Keanu");
        person.setLastName("Reeves");
        person.setId(30L);
        person.setBirthday(LocalDateTime.of(1964, Month.SEPTEMBER, 2, 10,0,0).toLocalDate());

        entityManager.persist(person);

        NodeWithoutGeneratedValue node = new NodeWithoutGeneratedValue();
        node.setId(30L);
        node.setBody("note");
        node.setPerson(person);

        entityManager.persist(node);

        Assertions.assertThrows(JdbcDaoException.class, () -> entityManager.find(PersonWithoutGeneratedValue.class, 10L));
        Assertions.assertDoesNotThrow(() -> entityManager.find(PersonWithoutGeneratedValue.class, person.getId()));
        var selectedPerson = entityManager.find(PersonWithoutGeneratedValue.class, person.getId());
        assertEquals(person.getFirstName(), selectedPerson.getFirstName());
        assertEquals(person.getLastName(), selectedPerson.getLastName());
        assertEquals(person.getBirthday(), selectedPerson.getBirthday());

        Assertions.assertThrows(JdbcDaoException.class, () -> entityManager.find(NodeWithoutGeneratedValue.class, 10L));
        Assertions.assertDoesNotThrow(() -> entityManager.find(NodeWithoutGeneratedValue.class, node.getId()));
        var selectedNote = entityManager.find(NodeWithoutGeneratedValue.class, node.getId());
        assertEquals(node.getBody(), selectedNote.getBody());
        assertEquals(node.getPerson().getId(), selectedNote.getPerson().getId());

        Assertions.assertDoesNotThrow(() -> entityManager.remove(selectedNote));
        Assertions.assertThrows(JdbcDaoException.class, () -> entityManager.find(NoteComplex.class, node.getId()));

        Assertions.assertDoesNotThrow(() -> entityManager.remove(selectedPerson));
        Assertions.assertThrows(JdbcDaoException.class, () -> entityManager.find(Person.class, person.getId()));

        entityTransaction.commit();
    }

}
