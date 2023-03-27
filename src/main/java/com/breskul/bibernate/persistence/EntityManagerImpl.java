package com.breskul.bibernate.persistence;

import com.breskul.bibernate.annotation.Id;
import com.breskul.bibernate.annotation.JoinColumn;
import com.breskul.bibernate.exception.JdbcDaoException;
import com.breskul.bibernate.persistence.util.DaoUtils;
import jakarta.persistence.EntityGraph;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.FlushModeType;
import jakarta.persistence.LockModeType;
import jakarta.persistence.Query;
import jakarta.persistence.StoredProcedureQuery;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaDelete;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.CriteriaUpdate;
import jakarta.persistence.metamodel.Metamodel;

import javax.sql.DataSource;
import java.lang.reflect.Field;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Queue;

import static com.breskul.bibernate.persistence.util.DaoUtils.getFieldByAnnotation;
import static com.breskul.bibernate.persistence.util.DaoUtils.getFieldValue;
import static com.breskul.bibernate.persistence.util.DaoUtils.isEntityCollectionField;
import static com.breskul.bibernate.persistence.util.DaoUtils.isEntityField;
import static com.breskul.bibernate.persistence.util.DaoUtils.isRegularField;
import static com.breskul.bibernate.persistence.util.DaoUtils.isValidEntity;
import static com.breskul.bibernate.persistence.util.DaoUtils.resolveFieldName;

public class EntityManagerImpl implements EntityManager {
    private final JdbcDao jdbcDao;

    public EntityManagerImpl(DataSource dataSource) {
        this.jdbcDao = new JdbcDao(dataSource);
    }

    @Override
    public void persist(Object entityToSave) {
        isValidEntity(entityToSave.getClass());
        List<String> columns = new ArrayList<>();
        List<Object> values = new ArrayList<>();
        Queue<Collection<?>> queue = new ArrayDeque<>();
        for (Field declaredField : entityToSave.getClass().getDeclaredFields()) {
            if (declaredField.isAnnotationPresent(Id.class)) {
                Field idField = getFieldByAnnotation(entityToSave.getClass(), Id.class);
                Object entityId = jdbcDao.resolveEntityId(entityToSave, idField);
                if (entityId != null) {
                    processRegularField(columns, values, idField, entityId);
                }
                continue;
            }
            Object fieldValue = getFieldValue(entityToSave, declaredField);
            if (isRegularField(declaredField)) {
                processRegularField(columns, values, declaredField, fieldValue);
            } else if (isEntityCollectionField(declaredField)) {
                processCollection(queue, fieldValue);
            } else if (isEntityField(declaredField)) {
                processEntityField(columns, values, declaredField, fieldValue);
            }
        }

        jdbcDao.executeInsert(entityToSave, values, columns);
        processActionQueue(queue);

    }


    private void processActionQueue(Queue<Collection<?>> queue) {
        queue.forEach(collection -> collection.forEach(this::persist));
    }

    private void processRegularField(List<String> columns, List<Object> values, Field declaredField, Object fieldValue) {
        columns.add(resolveFieldName(declaredField));
        values.add(fieldValue);
    }

    private void processCollection(Queue<Collection<?>> queue, Object fieldValue) {
        if (fieldValue != null) {
            queue.add((Collection<?>) fieldValue);
        }
    }

    private void processEntityField(List<String> columns, List<Object> values, Field declaredField, Object fieldValue) {
        Object relatedEntity = JdbcDao.ENTITY_ID_MAP.get(fieldValue);
        if (relatedEntity != null) {
            String name = declaredField.getAnnotation(JoinColumn.class).name();
            columns.add(name);
            values.add(relatedEntity);
        } else {
            throw new JdbcDaoException("Can't use transient entity here", "Make sure not to use transient entity in session");
        }
    }


    @Override
    public <T> T merge(T entity) {
        return null;
    }

    @Override
    public void remove(Object entity) {
        String tableName = DaoUtils.getClassTableName(entity.getClass());
        String identifierName = DaoUtils.getIdentifierFieldName(entity.getClass());
        Object identifierValue = DaoUtils.getIdentifierValue(entity);
        this.jdbcDao.deleteByIdentifier(tableName, identifierName, identifierValue);
    }

    @Override
    public <T> T find(Class<T> entityClass, Object primaryKey) {
        return null;
    }

    @Override
    public <T> T find(Class<T> entityClass, Object primaryKey, Map<String, Object> properties) {
        return null;
    }

    @Override
    public <T> T find(Class<T> entityClass, Object primaryKey, LockModeType lockMode) {
        return null;
    }

    @Override
    public <T> T find(Class<T> entityClass, Object primaryKey, LockModeType lockMode, Map<String, Object> properties) {
        return null;
    }

    @Override
    public <T> T getReference(Class<T> entityClass, Object primaryKey) {
        return null;
    }

    @Override
    public void flush() {

    }

    @Override
    public void setFlushMode(FlushModeType flushMode) {

    }

    @Override
    public FlushModeType getFlushMode() {
        return null;
    }

    @Override
    public void lock(Object entity, LockModeType lockMode) {

    }

    @Override
    public void lock(Object entity, LockModeType lockMode, Map<String, Object> properties) {

    }

    @Override
    public void refresh(Object entity) {

    }

    @Override
    public void refresh(Object entity, Map<String, Object> properties) {

    }

    @Override
    public void refresh(Object entity, LockModeType lockMode) {

    }

    @Override
    public void refresh(Object entity, LockModeType lockMode, Map<String, Object> properties) {

    }

    @Override
    public void clear() {

    }

    @Override
    public void detach(Object entity) {

    }

    @Override
    public boolean contains(Object entity) {
        return false;
    }

    @Override
    public LockModeType getLockMode(Object entity) {
        return null;
    }

    @Override
    public void setProperty(String propertyName, Object value) {

    }

    @Override
    public Map<String, Object> getProperties() {
        return null;
    }

    @Override
    public Query createQuery(String qlString) {
        return null;
    }

    @Override
    public <T> TypedQuery<T> createQuery(CriteriaQuery<T> criteriaQuery) {
        return null;
    }

    @Override
    public Query createQuery(CriteriaUpdate updateQuery) {
        return null;
    }

    @Override
    public Query createQuery(CriteriaDelete deleteQuery) {
        return null;
    }

    @Override
    public <T> TypedQuery<T> createQuery(String qlString, Class<T> resultClass) {
        return null;
    }

    @Override
    public Query createNamedQuery(String name) {
        return null;
    }

    @Override
    public <T> TypedQuery<T> createNamedQuery(String name, Class<T> resultClass) {
        return null;
    }

    @Override
    public Query createNativeQuery(String sqlString) {
        return null;
    }

    @Override
    public Query createNativeQuery(String sqlString, Class resultClass) {
        return null;
    }

    @Override
    public Query createNativeQuery(String sqlString, String resultSetMapping) {
        return null;
    }

    @Override
    public StoredProcedureQuery createNamedStoredProcedureQuery(String name) {
        return null;
    }

    @Override
    public StoredProcedureQuery createStoredProcedureQuery(String procedureName) {
        return null;
    }

    @Override
    public StoredProcedureQuery createStoredProcedureQuery(String procedureName, Class... resultClasses) {
        return null;
    }

    @Override
    public StoredProcedureQuery createStoredProcedureQuery(String procedureName, String... resultSetMappings) {
        return null;
    }

    @Override
    public void joinTransaction() {

    }

    @Override
    public boolean isJoinedToTransaction() {
        return false;
    }

    @Override
    public <T> T unwrap(Class<T> cls) {
        return null;
    }

    @Override
    public Object getDelegate() {
        return null;
    }

    @Override
    public void close() {

    }

    @Override
    public boolean isOpen() {
        return false;
    }

    @Override
    public EntityTransaction getTransaction() {
        return null;
    }

    @Override
    public EntityManagerFactory getEntityManagerFactory() {
        return null;
    }

    @Override
    public CriteriaBuilder getCriteriaBuilder() {
        return null;
    }

    @Override
    public Metamodel getMetamodel() {
        return null;
    }

    @Override
    public <T> EntityGraph<T> createEntityGraph(Class<T> rootType) {
        return null;
    }

    @Override
    public EntityGraph<?> createEntityGraph(String graphName) {
        return null;
    }

    @Override
    public EntityGraph<?> getEntityGraph(String graphName) {
        return null;
    }

    @Override
    public <T> List<EntityGraph<? super T>> getEntityGraphs(Class<T> entityClass) {
        return null;
    }
}
