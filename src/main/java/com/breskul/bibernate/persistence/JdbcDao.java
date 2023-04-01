package com.breskul.bibernate.persistence;

import com.breskul.bibernate.annotation.GeneratedValue;
import com.breskul.bibernate.annotation.Id;
import com.breskul.bibernate.annotation.Strategy;
import com.breskul.bibernate.exception.JdbcDaoException;
import com.breskul.bibernate.exception.ReflectionException;
import com.breskul.bibernate.exception.TransactionException;
import com.breskul.bibernate.persistence.util.CacheUtils;
import com.breskul.bibernate.persistence.util.DaoUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Supplier;

import static com.breskul.bibernate.persistence.util.DaoUtils.*;

public class JdbcDao {
    private static final Logger logger = LoggerFactory.getLogger(JdbcDao.class);

    private Connection connection;
    private final Map<EntityKey<?>, Object> cache;

    private static final String SELECT_FROM_TABLE_BY_COLUMN_STATEMENT = "SELECT %s.* FROM %s %s WHERE %s.%s = ?";
    private static final String DELETE_STATEMENT = "DELETE FROM %s WHERE %s = ?";
    private static final String INSERT_QUERY = "INSERT INTO %s (%s) VALUES (%s)";
    private static final String SELECT_SEQ_QUERY = "SELECT nextval('%s_seq')";
    private static final String H2_TABLE_NOT_FOUND_STATE = "42S02";
    private static final String POSTGRES_TABLE_NOT_FOUND_STATE = "42P01";

    public JdbcDao(Map<EntityKey<?>, Object> cache) {
        this.cache = cache;
    }

    public void setConnection(Connection connection) {
        this.connection = connection;
    }

    private Connection getConnection() {
        if (Objects.isNull(connection)) {
            throw new TransactionException("Transaction was not open", "Begin transaction before persist operations");
        }
        return connection;
    }

    public <T> Object findSequence(Class<T> type) {
        isValidEntity(type);
        String tableName = getClassTableName(type);
        String sequenceTable = SELECT_SEQ_QUERY.formatted(tableName);
        try (PreparedStatement preparedStatement = getConnection().prepareStatement(sequenceTable)) {
            ResultSet resultSet = preparedStatement.executeQuery();
            resultSet.next();
            return resultSet.getObject(1);
        } catch (SQLException e) {
            throw new JdbcDaoException("Can't find sequence %s".formatted(sequenceTable),
                    "Make sure that sequence match the pattern 'tableName_seq'", e);
        }
    }

    public <T> T findByIdentifier(Class<T> entityType, String tableName, Object identifier) {
        Field idField = DaoUtils.getIdentifierField(entityType);
        return findOneBy(entityType, tableName, idField, identifier);
    }

    public <T> List<T> findAllBy(Class<T> entityType, String tableName, Field field, Object columnValue) {
        final var alias = tableName.substring(0, 1).toLowerCase();
        var columnName = DaoUtils.getColumnName(field);
        String formattedDeleteStatement =
                String.format(SELECT_FROM_TABLE_BY_COLUMN_STATEMENT, alias, tableName, alias, alias, columnName);
        final var cause = String.format("Error occurred while executing 'SELECT BY %s' statement", columnName);
        var list = new ArrayList<T>();
        try (PreparedStatement preparedStatement = getConnection().prepareStatement(formattedDeleteStatement)) {
            preparedStatement.setObject(1, columnValue);
            logger.info("SQL: {}", preparedStatement);
            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                var entity = createEntityFromResultSet(entityType, resultSet);
                list.add(entity);
            }
        } catch (SQLException exception) {
            throw new JdbcDaoException(cause, exception);
        }
        return list;
    }

    public <T> T findOneBy(Class<T> entityType, String tableName, Field field, Object columnValue) {
        List<T> resultList = findAllBy(entityType, tableName, field, columnValue);
        if (resultList.size() > 1) {
            throw new JdbcDaoException("The result must contain exactly one row");
        } else if (resultList.size() == 1) {
            return resultList.get(0);
        } else {
            return null;
        }
    }

    public void deleteByIdentifier(String tableName, String identifierName, Object identifier) {
        String formattedDeleteStatement = String.format(DELETE_STATEMENT, tableName, identifierName);
        var cause = "error occurred while executing delete statement";
        var solution = "check your sql query";
        try (PreparedStatement preparedStatement = getConnection().prepareStatement(formattedDeleteStatement)) {
            preparedStatement.setObject(1, identifier);
            logger.info("SQL:" + preparedStatement);
            int rowDeleted = preparedStatement.executeUpdate();
            if (rowDeleted == 0) {
                throw new JdbcDaoException(cause);
            }
        } catch (SQLException e) {
            throw new JdbcDaoException(cause, solution, e);
        }
    }

    public void executeInsert(Object entityToSave, List<Object> values, List<String> columns) {
        Class<?> entityType = entityToSave.getClass();
        String tableName = getClassTableName(entityType);
        String insertQuery = formatQuery(tableName, INSERT_QUERY, columns, values);
        try (PreparedStatement preparedStatement = getConnection().prepareStatement(insertQuery, Statement.RETURN_GENERATED_KEYS)) {
            logger.info("SQL:" + preparedStatement);
            setPreparedValues(values, preparedStatement);
            preparedStatement.executeUpdate();
            preparedStatement.getGeneratedKeys().next();
            Object id = preparedStatement.getGeneratedKeys().getObject(1);
            setValueToField(entityToSave, id, Id.class);
            EntityKey<?> entityKey = EntityKey.of(entityType, id);
            cache.put(entityKey, entityToSave);
        } catch (SQLException e) {
            if (H2_TABLE_NOT_FOUND_STATE.equals(e.getSQLState()) || POSTGRES_TABLE_NOT_FOUND_STATE.equals(e.getSQLState())) {
                throw new JdbcDaoException("Table %s not found".formatted(tableName), "Use @Table annotation to specify table's name", e);
            } else {
                throw new JdbcDaoException("Can not execute query", "See stacktrace", e);
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
                    savedEntityId = findSequence(entityToSave.getClass());
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

    public <T> T createEntityFromResultSet(Class<T> entityType, ResultSet resultSet) throws SQLException {
        T entity;
        try {
            Constructor<T> constructor = entityType.getConstructor();
            entity = constructor.newInstance();
            for (var field : entityType.getDeclaredFields()) {
                field.setAccessible(true);
                if (DaoUtils.isRegularField(field)) {
                    logger.debug("Setting regular column field");
                    var columnName = DaoUtils.getColumnName(field);
                    Object columnValue = resultSet.getObject(columnName);
                    if (columnValue instanceof Timestamp tms) {
                        Class<?> fieldType = field.getType();
                        if (fieldType == LocalDateTime.class) {
                            field.set(entity, tms.toLocalDateTime());
                        } else if (fieldType == LocalDate.class) {
                            field.set(entity, tms.toLocalDateTime().toLocalDate());
                        }
                    } else {
                        field.set(entity, columnValue);
                    }
                } else if (isEntityField(field)) {
                    logger.debug("Setting toOne related entity");
                    var relatedEntityType = field.getType();
                    var relatedEntityTableName = DaoUtils.getClassTableName(relatedEntityType);
                    var joinColumnName = DaoUtils.resolveFieldName(field);
                    var joinColumnValue = resultSet.getObject(joinColumnName);
                    Supplier<?> fetchSupplier = () -> findByIdentifier(relatedEntityType, relatedEntityTableName, joinColumnValue);
                    EntityKey<?> entityKey = EntityKey.of(relatedEntityType, joinColumnValue);
                    var relatedEntity = CacheUtils.processCache(entityKey, cache, fetchSupplier);
                    field.set(entity, relatedEntity);
                } else if (isEntityCollectionField(field)) {
                    logger.debug("Setting lazy list for toMany related entities");
                }
            }
        } catch (InstantiationException exception) {
            throw new RuntimeException("It's not possible to create an instance of a class", exception);
        } catch (IllegalAccessException exception) {
            throw new JdbcDaoException("There is no access to the definition of the specified class, field, method or constructor",
                    "Set the accessible flag 'true'", exception);
        } catch (InvocationTargetException | NoSuchMethodException exception) {
            String className = entityType.getSimpleName();
            throw new ReflectionException(String.format("The is an issuer to create an instance of the class '%s'", className),
                    String.format("Check the existence of constructor in '%s' class", className), exception);
        }
        return entity;
    }
}
