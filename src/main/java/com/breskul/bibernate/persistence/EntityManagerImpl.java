package com.breskul.bibernate.persistence;

import com.breskul.bibernate.exception.EntityManagerException;
import com.breskul.bibernate.persistence.model.EntityKey;
import com.breskul.bibernate.persistence.util.CacheUtils;
import com.breskul.bibernate.persistence.util.DaoUtils;

import javax.sql.DataSource;
import java.lang.reflect.Field;
import java.util.*;
import java.util.function.Supplier;

import static com.breskul.bibernate.validate.EntityValidation.validateFetchEntity;
import static com.breskul.bibernate.validate.EntityValidation.validatePersistEntity;

public class EntityManagerImpl implements EntityManager {
    private final DataSource dataSource;
    private final JdbcDao jdbcDao;

    private transient EntityTransactionImpl entityTransaction;
    private final PersistenceContext context;

    private boolean isOpen;

    public EntityManagerImpl(DataSource dataSource) {
        this.dataSource = dataSource;
        this.context = new PersistenceContext();
        this.jdbcDao = new JdbcDao(context);
        this.isOpen = true;
    }

    private void validateSession() {
        if (!this.isOpen) {
            throw new EntityManagerException("Entity manager closed", "Need to create new EntityManager instance");
        }
    }

    public void persist(Object entity) {
        validateSession();
        validatePersistEntity(entity, context.getCache());
        this.jdbcDao.persist(entity);
    }

    @Override
    public <T> T merge(T entity) {
        validateSession();

        if (entity == null) {
            throw new EntityManagerException("Attempt to merge null entity", "Check entity");
        }

        Object id = DaoUtils.getIdentifierValue(entity);
        if (context.getCache().containsKey(EntityKey.of(entity.getClass(), id))) {
            return entity;
        }

        if (id == null) {
            persist(entity);
            detach(entity);
        }

        return mergeEntity(entity);
    }

    private <T> T mergeEntity(T entity) {
        Object id = DaoUtils.getIdentifierValue(entity);
        T newEntity = findOrCreateEntity((Class<T>) entity.getClass(), id);

        Arrays.stream(newEntity.getClass().getDeclaredFields())
                .forEach(field -> updateField(newEntity, entity, field));

        context.addToCache(newEntity.getClass(), id);
        return newEntity;
    }

    private <T> void updateField(T newEntity, T oldEntity, Field field) {
        if (DaoUtils.isRegularField(field)) {
            Object newEntityFieldValue = DaoUtils.getFieldValue(newEntity, field);
            Object oldEntityFieldValue = DaoUtils.getFieldValue(oldEntity, field);
            if (newEntityFieldValue == null || !newEntityFieldValue.equals(oldEntityFieldValue)) {
                DaoUtils.setValueToField(newEntity, oldEntityFieldValue, field);
            }
        } else if (DaoUtils.isEntityCollectionField(field)
                && DaoUtils.isFieldAllOrMergeCascade(field)) {
            Collection<Object> newEntityFieldValue = (Collection<Object>) DaoUtils.getFieldValue(newEntity, field);
            Collection<?> oldEntityFieldValue = (Collection<?>) DaoUtils.getFieldValue(oldEntity, field);
            newEntityFieldValue.clear();
            if (DaoUtils.isLoadedLazyList(oldEntityFieldValue)) {
                for (Object element : oldEntityFieldValue) {
                    newEntityFieldValue.add(merge(element));
                }
            }
        } else if (DaoUtils.isEntityField(field)) {
            Object oldEntityFieldValue = DaoUtils.getFieldValue(oldEntity, field);
            Object newEntityFieldValue = merge(oldEntityFieldValue);
            DaoUtils.setValueToField(newEntity, newEntityFieldValue, field);
        }
    }

    private <T> T findOrCreateEntity(Class<T> entityClass, Object id) {
        if (id == null) {
            return DaoUtils.createEntityInstance(entityClass);
        }
        T entity = find(entityClass, id);
        if (entity == null) {
            return DaoUtils.createEntityInstance(entityClass);
        }
        return entity;
    }

    @Override
    public void remove(Object entity) {
        validateSession();
        this.jdbcDao.remove(entity);
    }

    @Override
    public <T> T find(Class<T> entityClass, Object primaryKey) {
        validateSession();
        validateFetchEntity(entityClass);
        String tableName = DaoUtils.getClassTableName(entityClass);
        Supplier<?> fetchSupplier = () -> jdbcDao.findByIdentifier(entityClass, tableName, primaryKey);
        EntityKey<?> entityKey = EntityKey.of(entityClass, primaryKey);
        Object result = CacheUtils.processCache(entityKey, context.getCache(), fetchSupplier);
        if (Objects.nonNull(result)) {
            String snapshotValues = DaoUtils.getSqlFieldValues(result);
            context.addToSnapshot(result, primaryKey, snapshotValues);
        }
        return entityClass.cast(result);
    }

    @Override
    public void flush() {
        jdbcDao.compareSnapshots();
    }

    @Override
    public void clear() {
        context.clear();
    }

    @Override
    public void detach(Object entity) {
        Object id = DaoUtils.getIdentifierValue(entity);
        context.removeFromCache(entity.getClass(), id);
        context.removeSnapshot(entity.getClass(), id);
    }

    @Override
    public boolean contains(Object entity) {
        Object id = DaoUtils.getIdentifierValue(entity);
        EntityKey<?> entityKey = EntityKey.of(entity.getClass(), id);
        return context.getCache().containsKey(entityKey);
    }

    @Override
    public void close() {
        context.clear();
        this.isOpen = false;
    }

    @Override
    public boolean isOpen() {
        return isOpen;
    }

    @Override
    public EntityTransaction getTransaction() {
        validateSession();
        return accessTransaction();
    }

    private EntityTransaction accessTransaction() {
        if (this.entityTransaction == null) {
            this.entityTransaction = new EntityTransactionImpl(this.dataSource, this.jdbcDao, context);
        }
        return this.entityTransaction;
    }
}
