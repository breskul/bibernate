package com.breskul.bibernate.persistence;

import com.breskul.bibernate.exeptions.JdbcDaoException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class JdbcDao {
    private static final Logger logger = LoggerFactory.getLogger(JdbcDao.class);
    private final DataSource dataSource;
    private final String DELETE_STATEMENT = "DELETE FROM %s WHERE %s = ?";

    public JdbcDao(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void deleteByIdentifier(String tableName, String identifierName, Object identifier) {
        String formattedDeleteStatement = String.format(DELETE_STATEMENT, tableName, identifierName);
        var cause = "error occurred while executing delete statement";
        var solution = "check your sql query";
        try (Connection connection = dataSource.getConnection()) {
            PreparedStatement preparedStatement = connection.prepareStatement(formattedDeleteStatement);
            preparedStatement.setObject(1, identifier);
            logger.info(preparedStatement.toString());
            int rowDeleted = preparedStatement.executeUpdate();
            if (rowDeleted == 0){
                throw new JdbcDaoException(cause, solution);
            }
        } catch (SQLException e) {
            throw new JdbcDaoException(cause, solution, e);
        }
    }


}
