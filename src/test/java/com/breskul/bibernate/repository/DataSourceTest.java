package com.breskul.bibernate.repository;

import com.breskul.bibernate.AbstractDataSourceTest;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class DataSourceTest extends AbstractDataSourceTest {

    private final String INSERT = "INSERT INTO users (id, first_name, last_name) VALUES (?, ?, ?)";
    private final String SELECT = "SELECT * FROM users WHERE id = ?";

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
            preparedStatement.setLong(1, 1);
            preparedStatement.setString(2, "FirstName");
            preparedStatement.setString(3, "LastName");
            int result = preparedStatement.executeUpdate();
            assertEquals(1, result);
        }
    }

    @Test
    @Order(3)
    public void selectData() throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            PreparedStatement preparedStatement = connection.prepareStatement(SELECT);
            preparedStatement.setLong(1, 1);
            ResultSet resultSet = preparedStatement.executeQuery();
            assertTrue(resultSet.next());
        }
    }
}
