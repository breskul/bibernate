package com.breskul.bibernate.persistence;

import com.breskul.bibernate.annotation.GeneratedValue;
import com.breskul.bibernate.annotation.Strategy;
import com.breskul.bibernate.exception.JdbcDaoException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.breskul.bibernate.persistence.util.DaoUtils.getClassTableName;
import static com.breskul.bibernate.persistence.util.DaoUtils.getFieldValue;
import static com.breskul.bibernate.persistence.util.DaoUtils.isValidEntity;

public class JdbcDao {
    private static final Logger logger = LoggerFactory.getLogger(JdbcDao.class);
    private final DataSource dataSource;
    private static final String DELETE_STATEMENT = "DELETE FROM %s WHERE %s = ?";
    private static final String INSERT_QUERY = "INSERT INTO %s (%s) VALUES (%s)";
    private static final String SELECT_SEQ_QUERY = "SELECT nextval('%s_seq')";
    protected static final Map<Object, Object> ENTITY_ID_MAP = new HashMap<>();
    private static final String H2_TABLE_NOT_FOUND_STATE = "42S02";
    private static final String POSTGRES_TABLE_NOT_FOUND_STATE = "42P01";

    public JdbcDao(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public static <T> Object findSequence(Class<T> type, DataSource dataSource) {
        isValidEntity(type);
        String tableName = getClassTableName(type);
        String sequenceTable = SELECT_SEQ_QUERY.formatted(tableName);
        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sequenceTable)) {
            ResultSet resultSet = preparedStatement.executeQuery();
            resultSet.next();
            return resultSet.getObject(1);
        } catch (SQLException e) {
            throw new JdbcDaoException("Can't find sequence %s".formatted(sequenceTable), "Make sure that sequence match the pattern 'tableName_seq'", e);
        }
    }

    public void deleteByIdentifier(String tableName, String identifierName, Object identifier) {
        String formattedDeleteStatement = String.format(DELETE_STATEMENT, tableName, identifierName);
        var cause = "error occurred while executing delete statement";
        var solution = "check your sql query";
        try (Connection connection = dataSource.getConnection()) {
            PreparedStatement preparedStatement = connection.prepareStatement(formattedDeleteStatement);
            preparedStatement.setObject(1, identifier);
            logger.info("SQL:" + preparedStatement);
            int rowDeleted = preparedStatement.executeUpdate();
            if (rowDeleted == 0) {
                throw new JdbcDaoException(cause, solution);
            }
        } catch (SQLException e) {
            throw new JdbcDaoException(cause, solution, e);
        }
    }

    public void executeInsert(Object entityToSave, List<Object> values, List<String> columns) {
        String tableName = getClassTableName(entityToSave.getClass());
        String insertQuery = formatQuery(tableName, INSERT_QUERY, columns, values);
        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(insertQuery, Statement.RETURN_GENERATED_KEYS)) {
            setPreparedValues(values, preparedStatement);
            preparedStatement.executeUpdate();
            preparedStatement.getGeneratedKeys().next();
            Object next = preparedStatement.getGeneratedKeys().getObject(1);
            ENTITY_ID_MAP.put(entityToSave, next);
        } catch (SQLException e) {
            if (H2_TABLE_NOT_FOUND_STATE.equals(e.getSQLState()) || POSTGRES_TABLE_NOT_FOUND_STATE.equals(e.getSQLState())) {
                throw new JdbcDaoException("Table %s not found".formatted(tableName), "Use @Table annotation to specify table's name", e);
            }
        }
    }

    public Object resolveEntityId(Object entityToSave, Field idField) {
        Object savedEntityId = getFieldValue(entityToSave, idField);
        if (idField.isAnnotationPresent(GeneratedValue.class)) {
            if (savedEntityId != null) {
                throw new JdbcDaoException("detached entity passed to persist: " + entityToSave.getClass().getName(), "Make sure that you don't set id manually when using @GeneratedValue");
            } else {
                Strategy strategy = idField.getAnnotation(GeneratedValue.class).strategy();
                if (strategy.equals(Strategy.SEQUENCE)) {
                    savedEntityId = findSequence(entityToSave.getClass(), dataSource);
                }
            }
        } else if (savedEntityId == null) {
            throw new JdbcDaoException("No id present for %s".formatted(entityToSave.getClass().getName()), "ids for this class must be manually assigned before calling save(): " + entityToSave.getClass().getName());
        }
        return savedEntityId;
    }

    private String formatQuery(String tableName, String query, List<String> columns, List<Object> values) {
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < values.size(); i++) {
            stringBuilder.append("?");
            if (i != values.size() - 1) {
                stringBuilder.append(", ");
            }
        }

        return query.formatted(tableName, String.join(", ", columns), stringBuilder.toString());
    }

    private void setPreparedValues(List<Object> values, PreparedStatement preparedStatement) throws SQLException {
        for (int i = 0; i < values.size(); i++) {
            try {
                preparedStatement.setObject(i + 1, values.get(i));
            } catch (Exception e) {
                preparedStatement.setObject(i + 1, values.get(i), java.sql.Types.OTHER);
            }
        }
    }
}
