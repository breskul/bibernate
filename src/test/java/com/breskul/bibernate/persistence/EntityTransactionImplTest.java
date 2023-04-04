package com.breskul.bibernate.persistence;

import com.breskul.bibernate.AbstractDataSourceTest;
import com.breskul.bibernate.exception.TransactionException;
import com.breskul.bibernate.persistence.test_model.PersonWithoutIdAndStrategy;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.PreparedStatement;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.*;

public class EntityTransactionImplTest extends AbstractDataSourceTest {

    private EntityTransaction entityTransaction;
    private EntityManager entityManager;

    @BeforeEach
    void setUp() {
        entityManager = new EntityManagerImpl(dataSource);
        entityTransaction = entityManager.getTransaction();
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
    public void openTransaction() {
        assertFalse(entityTransaction.isActive());
        entityTransaction.begin();
        assertTrue(entityTransaction.isActive());
        entityTransaction.commit();
    }

    @Test
    public void openTwoTransaction() {
        entityTransaction.begin();
        assertThrows(TransactionException.class, () -> entityTransaction.begin());
    }
    @Test
    public void commitTransaction() {
        entityTransaction.begin();
        assertTrue(entityTransaction.isActive());

        PersonWithoutIdAndStrategy person = new PersonWithoutIdAndStrategy();
        person.setId(101L);
        person.setFirstName("FirstName");
        person.setLastName("LastName");

        entityManager.persist(person);

        entityTransaction.commit();
        assertFalse(entityTransaction.isActive());

        entityTransaction.begin();
        PersonWithoutIdAndStrategy selectedPerson = entityManager.find(PersonWithoutIdAndStrategy.class, person.getId());
        entityTransaction.commit();
        assertTrue(person.equals(selectedPerson));
    }


    @Test
    public void commitWithNotOpenedTransaction() {
        assertThrows(TransactionException.class, () -> entityTransaction.commit());
    }

    @Test
    public void setRollbackOnly() {
        entityTransaction.begin();
        entityTransaction.setRollbackOnly();
        assertTrue(entityTransaction.getRollbackOnly());

        PersonWithoutIdAndStrategy person = new PersonWithoutIdAndStrategy();
        person.setId(101L);
        person.setFirstName("FirstName");
        person.setLastName("LastName");

        entityManager.persist(person);

        entityTransaction.commit();
        assertFalse(entityTransaction.isActive());

        entityTransaction.begin();
        assertNull(entityManager.find(PersonWithoutIdAndStrategy.class, person.getId()));
        entityTransaction.commit();
    }

    @Test
    public void rollback() {
        entityTransaction.begin();

        PersonWithoutIdAndStrategy person = new PersonWithoutIdAndStrategy();
        person.setId(101L);
        person.setFirstName("FirstName");
        person.setLastName("LastName");

        entityManager.persist(person);

        entityTransaction.rollback();
        assertFalse(entityTransaction.isActive());

        entityTransaction.begin();
        assertNull(entityManager.find(PersonWithoutIdAndStrategy.class, person.getId()));
        entityTransaction.commit();
    }

}
