package com.breskul.bibernate.persistence;

import com.breskul.bibernate.AbstractDataSourceTest;
import com.breskul.bibernate.persistence.test_model.Person;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.sql.PreparedStatement;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.*;

public class EntityManagerCacheTest extends AbstractDataSourceTest {

    private final String INSERT = "INSERT INTO users (id, first_name, last_name) VALUES (?, ?, ?)";
    private final Long ID = 1001L;

    private EntityManager entityManager;

    @BeforeEach
    void setUp() {
        doInConnection(connection -> {
            try {
                PreparedStatement preparedStatement = connection.prepareStatement(INSERT);
                preparedStatement.setLong(1, ID);
                preparedStatement.setString(2, "FirstName");
                preparedStatement.setString(3, "LastName");
                preparedStatement.executeUpdate();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        });
        entityManager = new EntityManagerImpl(dataSource);
    }

    @AfterEach
    void destroy() {
        doInConnection(connection -> {
            try {
                PreparedStatement persons = connection.prepareStatement(CLEAN_PERSON_TABLE);
                persons.execute();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        });
        entityManager.close();
    }

    @Test
    @DisplayName("Test cache find")
    public void testCacheFind() {
        entityManager.getTransaction().begin();
        Person person1 = entityManager.find(Person.class, ID);
        Person person2 = entityManager.find(Person.class, ID);
        entityManager.getTransaction().commit();
        assertSame(person1, person2);
    }

    @Test
    @DisplayName("Test cache persist")
    public void testCachePersist() {
        entityManager.getTransaction().begin();
        Person person1 = new Person();
        person1.setFirstName("FirstName");
        person1.setLastName("LastName");
        entityManager.persist(person1);
        Person person2 = entityManager.find(Person.class, person1.getId());
        entityManager.getTransaction().commit();
        assertSame(person1, person2);
    }
}
