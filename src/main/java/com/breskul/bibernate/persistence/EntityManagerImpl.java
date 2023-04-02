package com.breskul.bibernate.persistence;

import com.breskul.bibernate.annotation.Id;
import com.breskul.bibernate.annotation.JoinColumn;
import com.breskul.bibernate.annotation.ManyToOne;
import com.breskul.bibernate.exception.EntityManagerException;
import com.breskul.bibernate.exception.JdbcDaoException;
import com.breskul.bibernate.exception.TransactionException;
import com.breskul.bibernate.persistence.util.CacheUtils;
import com.breskul.bibernate.persistence.util.DaoUtils;
import jakarta.persistence.*;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaDelete;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.CriteriaUpdate;
import jakarta.persistence.metamodel.Metamodel;

import javax.sql.DataSource;
import java.lang.reflect.Field;
import java.util.*;
import java.util.function.Supplier;

import static com.breskul.bibernate.persistence.util.DaoUtils.*;

public class EntityManagerImpl implements EntityManager {
    private final DataSource dataSource;
    private final JdbcDao jdbcDao;

    private transient EntityTransactionImpl entityTransaction;
    private final Map<EntityKey<?>, Object> cache = new HashMap<>();

    private boolean isOpen;

    public EntityManagerImpl(DataSource dataSource) {
        this.dataSource = dataSource;
        this.jdbcDao = new JdbcDao(cache);
        this.isOpen = true;
    }

    private void validateSession() {
        if (!this.isOpen) {
            throw new EntityManagerException("Entity manager closed", "Need to create new EntityManager instance");
        }
    }

    @Override
    public void persist(Object entityToSave) {
        validateSession();
        isValidEntity(entityToSave.getClass());
        List<String> columns = new ArrayList<>();
        List<Object> values = new ArrayList<>();
        Queue<Collection<?>> queue = new ArrayDeque<>();
        Class<?> entityType = entityToSave.getClass();
        for (Field declaredField : entityType.getDeclaredFields()) {
            if (declaredField.isAnnotationPresent(Id.class)) {
                Field idField = getFieldByAnnotation(entityType, Id.class);
                Object entityId = jdbcDao.resolveEntityId(entityToSave, idField);
                if (entityId != null) {
                    processRegularField(columns, values, idField, entityId);
                }
            } else {
                Object fieldValue = getFieldValue(entityToSave, declaredField);
                if (isRegularField(declaredField)) {
                    processRegularField(columns, values, declaredField, fieldValue);
                } else if (isEntityCollectionField(declaredField)) {
                    processCollection(queue, fieldValue);
                } else if (isEntityField(declaredField)) {
                    processEntityField(columns, values, declaredField, fieldValue);
                }
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
        if (fieldValue != null) {
            JoinColumn annotation = declaredField.getAnnotation(JoinColumn.class);
            String name = annotation.name();
            columns.add(name);

            Object id = getIdentifierValue(fieldValue);
            values.add(id);
        } else {
            if (!declaredField.getAnnotation(ManyToOne.class).optional()) {
                throw new JdbcDaoException("Can't use transient entity here", "Make sure not to use transient entity in session");
            }
        }
    }

    @Override
    public <T> T merge(T entity) {
        validateSession();
        Object id = getIdentifierValue(entity);
        Class<T> entityClass = (Class<T>) entity.getClass();
        T newEntity;
        if (cache.containsKey(new EntityKey<>(entityClass, id))) {
            newEntity = entity;
        } else {
            newEntity = find(entityClass, id);
            mergeEntities(newEntity, entity);
        }
        return newEntity;
    }

    private <T> void mergeEntities(T newEntity, T oldEntity) {
        for (Field declaredField : newEntity.getClass().getDeclaredFields()) {
            if (isRegularField(declaredField)) {
                Object newEntityFieldValue = getFieldValue(newEntity, declaredField);
                Object oldEntityFieldValue = getFieldValue(oldEntity, declaredField);
                if (!newEntityFieldValue.equals(oldEntityFieldValue)) {
                    setValueToField(newEntity, oldEntityFieldValue, declaredField);
                }
            } else if (isEntityCollectionField(declaredField)) {
                Collection<Object> newEntityFieldValue = (Collection<Object>)getFieldValue(newEntity, declaredField);
                Collection<?> oldEntityFieldValue = (Collection<?>)getFieldValue(oldEntity, declaredField);
                newEntityFieldValue.clear();
                // TODO: check if oldEntityFieldValue is LazyList then no merge required
                for (Object element : oldEntityFieldValue){
                    newEntityFieldValue.add(merge(element));
                }
            } else if (isEntityField(declaredField)) {
                Object oldEntityFieldValue = getFieldValue(oldEntity, declaredField);
                Object newEntityFieldValue = merge(oldEntityFieldValue);
                setValueToField(newEntity, newEntityFieldValue, declaredField);
            }
        }
    }

    @Override
    public void remove(Object entity) {
        validateSession();
        String tableName = DaoUtils.getClassTableName(entity.getClass());
        String identifierName = DaoUtils.getIdentifierFieldName(entity.getClass());
        Object identifierValue = DaoUtils.getIdentifierValue(entity);
        this.jdbcDao.deleteByIdentifier(tableName, identifierName, identifierValue);
        EntityKey<?> entityKey = EntityKey.of(entity.getClass(), identifierValue);
        cache.remove(entityKey);
    }

    @Override
    public <T> T find(Class<T> entityClass, Object primaryKey) {
        validateSession();
        String tableName = DaoUtils.getClassTableName(entityClass);
        Supplier<?> fetchSupplier = () -> jdbcDao.findByIdentifier(entityClass, tableName, primaryKey);
        EntityKey<?> entityKey = EntityKey.of(entityClass, primaryKey);
        Object result = CacheUtils.processCache(entityKey, cache, fetchSupplier);
        return entityClass.cast(result);
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
        cache.clear();
        this.isOpen = false;
    }

    @Override
    public boolean isOpen() {
        return isOpen;
    }

    /**
     * Return EntityTransaction.
     *
     * @return EntityTransaction interface
     * @throws TransactionException if can not get connection
     **/
    @Override
    public EntityTransaction getTransaction() {
        validateSession();
        return accessTransaction();
    }

    private EntityTransaction accessTransaction() {
        if (this.entityTransaction == null) {
            this.entityTransaction = new EntityTransactionImpl(this.dataSource, this.jdbcDao, cache);
        }
        return this.entityTransaction;
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
