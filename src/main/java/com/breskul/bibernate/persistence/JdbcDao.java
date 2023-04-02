package com.breskul.bibernate.persistence;

import com.breskul.bibernate.annotation.Strategy;
import com.breskul.bibernate.exception.JdbcDaoException;
import com.breskul.bibernate.exception.ReflectionException;
import com.breskul.bibernate.exception.TransactionException;
import com.breskul.bibernate.persistence.util.DaoUtils;
import com.breskul.bibernate.persistence.util.Node;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

import static com.breskul.bibernate.persistence.util.DaoUtils.*;

public class JdbcDao {

    private static final Logger logger = LoggerFactory.getLogger(JdbcDao.class);

    private Connection connection;
    private final Map<EntityKey<?>, Object> cache;

    private static final String SELECT_FROM_TABLE_BY_COLUMN_STATEMENT = "SELECT %s.* FROM %s %s WHERE %s.%s = ?";
    private static final String DELETE_STATEMENT = "DELETE FROM %s WHERE %s = ?";
    private static final String INSERT_QUERY = "INSERT INTO %s (%s) VALUES (%s)";
    private static final String SELECT_SEQ_QUERY = "SELECT nextval('%s_seq')";

    public JdbcDao(Map<EntityKey<?>, Object> cache) {
        this.cache = cache;
    }

    public void persist(Object parentEntity) {
        Node parentNode = buildTree(parentEntity);
        var queue = new ArrayDeque<Node>();
        queue.add(parentNode);
        while (!queue.isEmpty()) {
            var node = queue.poll();
            var entity = node.entity();
            var tableName = DaoUtils.resolveTableName(entity);
            var sqlFieldNames = DaoUtils.getSqlFieldNames(entity);
            var sqlFieldValues = DaoUtils.getSqlFieldValues(entity);

            var identifierField = DaoUtils.getIdentifierField(entity.getClass());
            var strategy = getStrategy(entity);
            Object id;
            if (strategy.equals(Strategy.SEQUENCE)) {
                var formattedSequenceQuery = String.format(SELECT_SEQ_QUERY, tableName);
                id = getSequenceId(formattedSequenceQuery);
                sqlFieldNames = identifierField.getName() + "," + sqlFieldNames;
                sqlFieldValues = id + "," + sqlFieldValues;
                identifierField.setAccessible(true);
                try {
                    identifierField.set(entity, id);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException(e);
                }
                insertEntity(tableName, sqlFieldNames, sqlFieldValues);
            } else if (strategy.equals(Strategy.IDENTITY)){
                id = insertEntity(tableName, sqlFieldNames, sqlFieldValues);
                identifierField.setAccessible(true);
                try {
                    identifierField.set(entity, id);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException(e);
                }
            } else {
                String idName = getIdentifierFieldName(entity.getClass());
                id = getIdentifierValue(entity);
                sqlFieldNames = idName + "," + sqlFieldNames;
                sqlFieldValues = id + "," + sqlFieldValues;

                insertEntity(tableName, sqlFieldNames, sqlFieldValues);
            }
            var entityKey = EntityKey.of(entity.getClass(), id);
            cache.put(entityKey, entity);
            queue.addAll(node.childes());

        }

    }

    private Node buildTree(Object entityToSave) {
        Node parentNode = new Node(entityToSave, new ArrayList<>());
        var queue = new ArrayDeque<Node>();
        queue.add(parentNode);
        while (!queue.isEmpty()) {
            var currentNode = queue.poll();
            var currentEntity = currentNode.entity();
            var childes = currentNode.childes();
            List<Field> collectionFieldList = DaoUtils.getCollectionFields(currentEntity.getClass());
            for (Field collectionField : collectionFieldList) {
                if (DaoUtils.isCollectionField(collectionField)) {
                    var childEntities = (Collection<?>) DaoUtils.getFieldValue(currentEntity, collectionField);
                    for (var childEntity : childEntities) {
                        var newNode = new Node(childEntity, new ArrayList<>());
                        childes.add(newNode);
                        queue.add(newNode);
                    }
                }
            }
        }
        return parentNode;
    }

    public Long getSequenceId(String sequenceQuery) {
        try (PreparedStatement preparedStatement = getConnection().prepareStatement(sequenceQuery)) {
            ResultSet resultSet = preparedStatement.executeQuery();
            resultSet.next();
            return resultSet.getLong(1);
        } catch (SQLException e) {
            throw new JdbcDaoException("Can't execute query %s".formatted(sequenceQuery), "Make sure that sequence match the pattern 'tableName_seq'", e);
        }
    }

    private Long insertEntity(String tableName, String sqlFieldNames, String sqlFieldValues) {
        var formattedInsertSql = String.format(INSERT_QUERY, tableName, sqlFieldNames, sqlFieldValues);
        try (PreparedStatement preparedStatement = getConnection().prepareStatement(formattedInsertSql, Statement.RETURN_GENERATED_KEYS)) {
            preparedStatement.executeUpdate();
            preparedStatement.getGeneratedKeys().next();
            return preparedStatement.getGeneratedKeys().getLong(1);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private Connection getConnection() {
        if (Objects.isNull(connection)) {
            throw new TransactionException("Transaction was not open", "Begin transaction before persist operations");
        }
        return connection;
    }

    public void setConnection(Connection connection) {
        this.connection = connection;
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
                    var relatedEntity = findByIdentifier(relatedEntityType, relatedEntityTableName, joinColumnValue);
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
