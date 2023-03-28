package com.breskul.bibernate.persistence;

import com.breskul.bibernate.AbstractDataSourceTest;
import com.breskul.bibernate.exception.JdbcDaoException;
import com.breskul.bibernate.persistence.testmodel.Person;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.ClassOrderer;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestClassOrder;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.function.Consumer;

@TestClassOrder(ClassOrderer.OrderAnnotation.class)
public class EntityManagerImplTest extends AbstractDataSourceTest {

    private static final String NEW_PERSON_QUERY = "insert into users (id, first_name, last_name) values (10, 'Cat', 'Schrödinger')";

    @Test
    @Order(1)
    @DisplayName("1. Test remove method")
    public void testRemoveMethod() {
        EntityManager entityManager = new EntityManagerImpl(dataSource);
        Person person = new Person();
        person.setId(10L);
        person.setFirstName("user");
        Assertions.assertThrows(JdbcDaoException.class, () -> entityManager.remove(person));

        doInConnection(connection -> {
            try {
                PreparedStatement newNote = connection.prepareStatement(NEW_PERSON_QUERY);
                newNote.execute();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        });
        Assertions.assertDoesNotThrow(() -> entityManager.remove(person));

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
