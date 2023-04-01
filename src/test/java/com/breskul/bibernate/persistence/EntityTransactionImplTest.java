package com.breskul.bibernate.persistence;

import com.breskul.bibernate.AbstractDataSourceTest;
import com.breskul.bibernate.exception.JdbcDaoException;
import com.breskul.bibernate.exception.TransactionException;
import com.breskul.bibernate.persistence.testmodel.PersonWithoutIdAndStrategy;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import java.sql.PreparedStatement;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
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
    @Order(1)
    public void openTransaction() {
        assertFalse(entityTransaction.isActive());
        entityTransaction.begin();
        assertTrue(entityTransaction.isActive());
        entityTransaction.commit();
    }

    @Test
    @Order(2)
    public void openTwoTransaction() {
        entityTransaction.begin();
        assertThrows(TransactionException.class, () -> entityTransaction.begin());
    }
    @Test
    @Order(3)
    public void commitTransaction() {
        entityTransaction.begin();
        assertTrue(entityTransaction.isActive());

        PersonWithoutIdAndStrategy person = new PersonWithoutIdAndStrategy();
        person.setId(101L);
        person.setFirstName("FirstName");
        person.setLastName("LastName");

        assertThrows(JdbcDaoException.class, () -> entityManager.persist(person));


        entityTransaction.commit();
        assertFalse(entityTransaction.isActive());

//        entityTransaction.begin();
//        PersonWithoutIdAndStrategy selectedPerson = entityManager.find(PersonWithoutIdAndStrategy.class, person.getId());
//        entityTransaction.commit();
//        assertTrue(person.equals(selectedPerson));
    }


    @Test
    @Order(4)
    public void commitWithNotOpenedTransaction() {
        assertThrows(TransactionException.class, () -> entityTransaction.commit());
    }

    @Test
    @Order(5)
    public void setRollbackOnly() {
        entityTransaction.begin();
        entityTransaction.setRollbackOnly();
        assertTrue(entityTransaction.getRollbackOnly());

        PersonWithoutIdAndStrategy person = new PersonWithoutIdAndStrategy();
        person.setId(101L);
        person.setFirstName("FirstName");
        person.setLastName("LastName");

        assertThrows(JdbcDaoException.class, () -> entityManager.persist(person));
        entityTransaction.commit();

//        entityTransaction.commit();
//        assertFalse(entityTransaction.isActive());
//
//        entityTransaction.begin();
//        Assertions.assertThrows(JdbcDaoException.class, () -> entityManager.find(PersonWithoutIdAndStrategy.class, person.getId()));
//        entityTransaction.commit();
    }

    @Test
    @Order(5)
    public void rollback() {
        entityTransaction.begin();

        PersonWithoutIdAndStrategy person = new PersonWithoutIdAndStrategy();
        person.setId(101L);
        person.setFirstName("FirstName");
        person.setLastName("LastName");

        assertThrows(JdbcDaoException.class, () -> entityManager.persist(person));
        entityTransaction.commit();

//        entityTransaction.rollback();
//        assertFalse(entityTransaction.isActive());
//
//        entityTransaction.begin();
//        Assertions.assertThrows(JdbcDaoException.class, () -> entityManager.find(PersonWithoutIdAndStrategy.class, person.getId()));
//        entityTransaction.commit();
    }

}
