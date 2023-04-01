package com.breskul.bibernate.repository;

import com.breskul.bibernate.AbstractDataSourceTest;
import com.breskul.bibernate.persistence.EntityManagerImpl;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.*;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class DataSourceTest extends AbstractDataSourceTest {

    private final String INSERT = "INSERT INTO users (id, first_name, last_name) VALUES (?, ?, ?)";
    private final String SELECT = "SELECT * FROM users WHERE id = ?";
    private final String DELETE = "DELETE FROM users WHERE id = ?";

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
    public void dataSourceNotNull() {
        assertNotNull(dataSource);
    }

    @Test
    @Order(2)
    public void insertData() throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            PreparedStatement preparedStatement = connection.prepareStatement(INSERT);
            preparedStatement.setLong(1, 1L);
            preparedStatement.setString(2, "FirstName");
            preparedStatement.setString(3, "LastName");
            int result = preparedStatement.executeUpdate();
            assertEquals(1, result);

            preparedStatement = connection.prepareStatement(DELETE);
            preparedStatement.setLong(1, 1L);
            result = preparedStatement.executeUpdate();
            assertEquals(1, result);
        }
    }

    @Test
    @Order(3)
    public void selectData() throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            PreparedStatement preparedStatement = connection.prepareStatement(INSERT);
            preparedStatement.setLong(1, 2L);
            preparedStatement.setString(2, "FirstName");
            preparedStatement.setString(3, "LastName");
            int result = preparedStatement.executeUpdate();
            assertEquals(1, result);

            preparedStatement = connection.prepareStatement(SELECT);
            preparedStatement.setLong(1, 2L);
            ResultSet resultSet = preparedStatement.executeQuery();
            assertTrue(resultSet.next());

            preparedStatement = connection.prepareStatement(DELETE);
            preparedStatement.setLong(1, 2L);
            result = preparedStatement.executeUpdate();
            assertEquals(1, result);
        }
    }
}
