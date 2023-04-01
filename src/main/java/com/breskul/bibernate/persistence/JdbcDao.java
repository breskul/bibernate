package com.breskul.bibernate.persistence;

import com.breskul.bibernate.exception.JdbcDaoException;
import com.breskul.bibernate.exception.TransactionException;
import com.breskul.bibernate.persistence.util.DaoUtils;
import com.breskul.bibernate.persistence.util.Node;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.sql.*;
import java.util.*;

import static com.breskul.bibernate.persistence.util.DaoUtils.*;

@Slf4j
public class JdbcDao {
    private static final Logger logger = LoggerFactory.getLogger(JdbcDao.class);
    private static final String DELETE_STATEMENT = "DELETE FROM %s WHERE %s = ?";
    private static final String INSERT_QUERY = "INSERT INTO %s (%s) VALUES (%s)";
    private static final String SELECT_SEQ_QUERY = "SELECT nextval('%s_seq')";
    private Connection connection;

    public void setConnection(Connection connection) {
        this.connection = connection;
    }

    public void persist(Object parentEntity)  {
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
            if (isSequenceStrategy(entity)) {
                var formattedSequenceQuery = String.format(SELECT_SEQ_QUERY, tableName);
                long id = getSequenceId(formattedSequenceQuery);
                sqlFieldNames = identifierField.getName() + "," + sqlFieldNames;
                sqlFieldValues = id + "," + sqlFieldValues;
            }
            long id = insertEntity(tableName, sqlFieldNames, sqlFieldValues);
            identifierField.setAccessible(true);
            try {
                identifierField.set(entity, id);
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
            queue.addAll(node.childes());



        }

    }
    private Connection getConnection() {
        if (Objects.isNull(connection)) {
            throw new TransactionException("Transaction was not open", "Begin transaction before persist operations");
        }
        return connection;
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

    public void deleteByIdentifier(String tableName, String identifierName, Object identifier) {
        String formattedDeleteStatement = String.format(DELETE_STATEMENT, tableName, identifierName);
        var cause = "error occurred while executing delete statement";
        var solution = "check your sql query";
        try (PreparedStatement preparedStatement = getConnection().prepareStatement(formattedDeleteStatement)) {
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
}
