package com.breskul.bibernate.persistence;

import com.breskul.bibernate.annotation.Strategy;
import com.breskul.bibernate.collection.LazyList;
import com.breskul.bibernate.exception.JdbcDaoException;
import com.breskul.bibernate.exception.ReflectionException;
import com.breskul.bibernate.exception.TransactionException;
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

    /**
     * <p>This method persists the given entity along with all of its dependent entities into the database. The entity to persist is passed as a parameter to this method.</p>
     *
     * @param parentEntity {@link Object} the JPA entity for which the list of column names should be returned.
     */
    public void persist(Object parentEntity) {
        EntityToInsertNode parentEntityToInsertNode = buildTreeDependencyFromParentEntity(parentEntity);
        var queue = new ArrayDeque<EntityToInsertNode>();
        queue.add(parentEntityToInsertNode);
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
                setIdentifierInEntity(entity, identifierField, id);
                insertEntity(tableName, sqlFieldNames, sqlFieldValues);
            } else if (strategy.equals(Strategy.IDENTITY)) {
                id = insertEntity(tableName, sqlFieldNames, sqlFieldValues);
                setIdentifierInEntity(entity, identifierField, id);
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

    /**
     * <p>This method sets the identifier field of the given entity with the given identifier value.</p>
     *
     * @param entity          {@link Object} entity in which identifier value will be set
     * @param identifierField {@link Field} identifier field
     * @param id              {@link Object} value which would be inserted into entity
     */
    private static void setIdentifierInEntity(Object entity, Field identifierField, Object id) {
        identifierField.setAccessible(true);
        try {
            identifierField.set(entity, id);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * <p>This method builds a tree of dependent entities starting from the given entity, which is used to determine the order in which the entities should be persisted.</p>
     *
     * @param entityToSave {@link Object} the JPA entity for which the list of column names should be returned.
     * @return {@link EntityToInsertNode} the parent node with all childes dependencies entities
     */
    private EntityToInsertNode buildTreeDependencyFromParentEntity(Object entityToSave) {
        EntityToInsertNode parentEntityToInsertNode = new EntityToInsertNode(entityToSave, new ArrayList<>());
        var queue = new ArrayDeque<EntityToInsertNode>();
        queue.add(parentEntityToInsertNode);
        while (!queue.isEmpty()) {
            var currentNode = queue.poll();
            var currentEntity = currentNode.entity();
            var childes = currentNode.childes();
            List<Field> collectionFieldList = DaoUtils.getCollectionFields(currentEntity.getClass());
            for (Field collectionField : collectionFieldList) {
                if (DaoUtils.isCollectionField(collectionField)) {
                    var childEntities = (Collection<?>) DaoUtils.getFieldValue(currentEntity, collectionField);
                    for (var childEntity : childEntities) {
                        var newNode = new EntityToInsertNode(childEntity, new ArrayList<>());
                        childes.add(newNode);
                        queue.add(newNode);
                    }
                }
            }
        }
        return parentEntityToInsertNode;
    }

    /**
     * <p>This method executes the given sequence query and returns the next value from the sequence.</p>
     *
     * @param sequenceQuery {@link String} formatted query executed in database
     * @return id {@link Object} from database
     */
    public Object getSequenceId(String sequenceQuery) {
        try (PreparedStatement preparedStatement = getConnection().prepareStatement(sequenceQuery)) {
            ResultSet resultSet = preparedStatement.executeQuery();
            resultSet.next();
            return resultSet.getObject(1);
        } catch (SQLException e) {
            throw new JdbcDaoException("Can't execute query %s".formatted(sequenceQuery), "Make sure that sequence match the pattern 'tableName_seq'", e);
        }
    }

    /**
     * <p>This method inserts an entity into the specified table in the database and returns the generated identifier value.</p>
     *
     * @param tableName      {@link String} - name of the table
     * @param sqlFieldNames  {@link String} - name of the sql field names for the given entity
     * @param sqlFieldValues {@link String} - values of the sql field names for the given entity
     * @return id {@link Object} - identifier for the inserted entity
     */
    private Object insertEntity(String tableName, String sqlFieldNames, String sqlFieldValues) {
        var formattedInsertSql = String.format(INSERT_QUERY, tableName, sqlFieldNames, sqlFieldValues);
        try (PreparedStatement preparedStatement = getConnection().prepareStatement(formattedInsertSql, Statement.RETURN_GENERATED_KEYS)) {
            logger.info("SQL: {}", preparedStatement);
            preparedStatement.executeUpdate();
            preparedStatement.getGeneratedKeys().next();
            return preparedStatement.getGeneratedKeys().getObject(1);
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

    /**
     * <p>Finds an entity by its identifier</p>
     *
     * @param entityType {@link Class} the class of the entity to find
     * @param tableName  {@link String} the name of the table in which to search for the entity
     * @param identifier {@link Object} the identifier of the entity to find
     * @param <T>        the type of the entity to find
     * @return the entity if found, null otherwise
     */
    public <T> T findByIdentifier(Class<T> entityType, String tableName, Object identifier) {
        Field idField = DaoUtils.getIdentifierField(entityType);
        return findOneBy(entityType, tableName, idField, identifier);
    }

    /**
     * <p>Finds all entities of the given class that have a field with a given value</p>
     *
     * @param entityType   {@link Class}the class of the entity to find
     * @param tableName    {@link String}the name of the table in which to search for the entities
     * @param field        {@link Field}the field in which to search for the value
     * @param columnValue  {@link Object} the value to search for
     * @param fieldsToSkip set of {@link Field}s to skip from loading to exclude circular dependency.
     * @param <T>          the type of the entity to find
     * @return a list {@link List} of entities that have the given value in the given field
     */
    public <T> List<T> findAllBy(Class<T> entityType, String tableName, Field field, Object columnValue, Set<Field> fieldsToSkip) {
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
                var entity = createEntityFromResultSet(entityType, resultSet, fieldsToSkip);
                list.add(entity);
            }
        } catch (SQLException exception) {
            throw new JdbcDaoException(cause, exception);
        }
        return list;
    }

    /**
     * <p>Finds an entity of the given class that has a field with a given value</p>
     *
     * @param entityType  {@link Class} the class of the entity to find
     * @param tableName   {@link String} the name of the table in which to search for the entity
     * @param field       {@link Field} the field in which to search for the value
     * @param columnValue {@link Object} the value to search for
     * @param <T>         the type of the entity to find
     * @return the entity if found, null otherwise
     */
    public <T> T findOneBy(Class<T> entityType, String tableName, Field field, Object columnValue) {
        List<T> resultList = findAllBy(entityType, tableName, field, columnValue, Collections.EMPTY_SET);
        if (resultList.size() > 1) {
            throw new JdbcDaoException("The result must contain exactly one row");
        } else if (resultList.size() == 1) {
            return resultList.get(0);
        } else {
            return null;
        }
    }

    /**
     * <p>takes a parent entity and a cache of related entities as parameters and uses a
     * stack-based algorithm to determine the order in which entities should be deleted to avoid violating foreign key constraints.</p>
     * @param parentEntity - {@link Object} entity to be deleted
     * @param cache - {@link Map} cache of current entities. Once entity is deleted, it is removed from cache
     */
    public void remove(Object parentEntity, Map<EntityKey<?>, Object> cache) {
        var stack = buildStackOfEntitiesToDelete(parentEntity);
        var cause = "could not execute your delete statement";
        while (!stack.isEmpty()){
            var entity = stack.pop();
            var tableName = DaoUtils.getClassTableName(entity.getClass());
            var identifierName = DaoUtils.getIdentifierFieldName(entity.getClass());
            var identifierValue = DaoUtils.getIdentifierValue(entity);
            var formattedSqlQuery = String.format(DELETE_STATEMENT, tableName, identifierName);
            try (PreparedStatement preparedStatement = getConnection().prepareStatement(formattedSqlQuery)) {
                preparedStatement.setObject(1, identifierValue);
                logger.info("SQL: {}", preparedStatement);
                if (preparedStatement.executeUpdate() != 1){
                    throw new JdbcDaoException(cause);
                }
                EntityKey<?> entityKey = EntityKey.of(entity.getClass(), identifierValue);
                cache.remove(entityKey);
            } catch (SQLException exception) {
                throw new JdbcDaoException(cause, exception);
            }

        }

    }
    /**
     * <p>returns a stack of entities to be deleted in the order in which they should be deleted. Child entities are deleted first</p>
     * @param parentEntity - {@link Object} root node of the tree
     * @return {@link Stack} of entities
     */
    private Stack<Object> buildStackOfEntitiesToDelete(Object parentEntity){
        var stack = new Stack<>();
        var queue = new ArrayDeque<>();
        queue.add(parentEntity);
        stack.add(parentEntity);
        while (!queue.isEmpty()){
            var currentEntity = queue.poll();
            List<Field> collecionFieldList = DaoUtils.getCascadeAllOrRemoveListFields(currentEntity.getClass());
            for (Field collectionField: collecionFieldList){
                var childEntities = (Collection<?>) DaoUtils.getFieldValue(currentEntity, collectionField);
                queue.addAll(childEntities);
                stack.addAll(childEntities);
            }

        }
        return stack;
    }

    /**
     * <p>creates a Java object of a given entity class from the ResultSet obtained from a database query.</p>
     *
     * @param entityType   {@link Class} A class object representing the type of the entity to be created.
     * @param resultSet    {@link ResultSet} object representing the result set obtained from a database query.
     * @param fieldsToSkip set of {@link Field}s to skip from loading to exclude circular dependency.
     * @return {@link Object} returns the entity representation of the sql row entry in database
     */
    public <T> T createEntityFromResultSet(Class<T> entityType, ResultSet resultSet, Set<Field> fieldsToSkip) throws SQLException {
        T entity;
        try {
            Constructor<T> constructor = entityType.getConstructor();
            entity = constructor.newInstance();
            for (var field : entityType.getDeclaredFields()) {
                if (fieldsToSkip == null || fieldsToSkip.isEmpty() || !fieldsToSkip.contains(field)) {
                    field.setAccessible(true);
                    if (DaoUtils.isRegularField(field)) {
                        logger.debug("Setting regular column field");
                        field.set(entity, setSimpleFieldValue(resultSet, field));
                    } else if (isEntityField(field)) {
                        logger.debug("Setting toOne related entity");
                        field.set(entity, getSingleEntityFieldValue(resultSet, field));
                    } else if (isEntityCollectionField(field)) {
                        logger.debug("Setting lazy list for toMany related entities");
                        field.set(entity, getCollectionEntityFieldValue(entityType, entity, field));
                    }
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

    private static Object setSimpleFieldValue(ResultSet resultSet, Field field) throws SQLException {
        Object result;
        var columnName = DaoUtils.getColumnName(field);
        Object columnValue = resultSet.getObject(columnName);
        if (columnValue instanceof Timestamp tms) {
            Class<?> fieldType = field.getType();
            if (fieldType == LocalDateTime.class) {
                result = tms.toLocalDateTime();
            } else if (fieldType == LocalDate.class) {
                result = tms.toLocalDateTime().toLocalDate();
            } else {
                result = columnValue;
            }
        } else {
            result = columnValue;
        }
        return result;
    }

    private Object getSingleEntityFieldValue(ResultSet resultSet, Field field) throws SQLException {
        var relatedEntityType = field.getType();
        var relatedEntityTableName = DaoUtils.getClassTableName(relatedEntityType);
        var joinColumnName = DaoUtils.resolveFieldName(field);
        var joinColumnValue = resultSet.getObject(joinColumnName);
        return findByIdentifier(relatedEntityType, relatedEntityTableName, joinColumnValue);
    }

    private <T> List<T> getCollectionEntityFieldValue(Class<T> entityType, T entity, Field field) {
        List<T> resultList;
        var relatedEntityType = DaoUtils.getEntityCollectionElementType(field);
        var relatedEntityTableName = DaoUtils.getClassTableName(relatedEntityType);
        var entityFieldInRelatedEntity = DaoUtils.getRelatedEntityField(entityType, relatedEntityType);
        var entityId = DaoUtils.getIdentifierValue(entity);
        var relatedEntityFieldsToSkip = Collections.singleton(entityFieldInRelatedEntity);
        if (DaoUtils.isEntityCollectionFieldIsLazy(field)) {
            resultList = new LazyList<>(() -> findAllBy(relatedEntityType, relatedEntityTableName, entityFieldInRelatedEntity, entityId, relatedEntityFieldsToSkip));
        } else {
            resultList = (List<T>) findAllBy(relatedEntityType, relatedEntityTableName, entityFieldInRelatedEntity, entityId, relatedEntityFieldsToSkip);
        }
        return resultList;
    }


}
