package com.breskul.bibernate.persistence;

import com.breskul.bibernate.configuration.PersistenceProperties;
import com.breskul.bibernate.exeptions.JdbcDaoException;
import com.breskul.bibernate.persistence.testModel.Person;
import com.breskul.bibernate.repository.DataSourceFactory;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.*;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.function.Consumer;

@TestClassOrder(ClassOrderer.OrderAnnotation.class)
public class EntityManagerImplTest {

    private static final String NEW_PERSON_QUERY = "insert into users (id, first_name, last_name) values (1, 'Cat', 'Schrödinger')";
    private static DataSource dataSource;

    @Test
    @Order(1)
    @DisplayName("1. Test remove method")
    public void testRemoveMethod() {
        PersistenceProperties.initialize();
        DataSourceFactory dataSourceFactory = DataSourceFactory.getInstance();
        dataSource = dataSourceFactory.getDataSource();
        EntityManager entityManager = new EntityMangerImpl(dataSource);
        Person person = new Person();
        person.setId(1L);
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

    private static void doInConnection(Consumer<Connection> consumer) {
        try (Connection connection = dataSource.getConnection()) {
            consumer.accept(connection);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }


}
