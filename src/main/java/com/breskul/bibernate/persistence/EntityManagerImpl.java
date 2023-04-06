package com.breskul.bibernate.persistence;

import com.breskul.bibernate.exception.EntityManagerException;
import com.breskul.bibernate.exception.TransactionException;
import com.breskul.bibernate.persistence.model.EntityKey;
import com.breskul.bibernate.persistence.util.CacheUtils;
import com.breskul.bibernate.persistence.util.DaoUtils;
import jakarta.persistence.*;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaDelete;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.CriteriaUpdate;
import jakarta.persistence.metamodel.Metamodel;

import javax.sql.DataSource;
import java.util.*;
import java.util.function.Supplier;

import static com.breskul.bibernate.validate.EntityValidation.validateFetchEntity;
import static com.breskul.bibernate.validate.EntityValidation.validatePersistEntity;

/**
 * <h2>This is the implementation class of the EntityManager interface. This class provides an implementation of the basic operations that can be performed on entities.</h2>
 * <p>Following fields are present:</p>
 * <ul>
 *     <li>dataSource: The {@link DataSource} object that is used to connect to the database.</li>
 *     <li>jdbcDao: The {@link JdbcDao} object that is used to execute SQL queries and interact with the database.</li>
 *     <li>entityTransaction: The {@link EntityTransactionImpl} object that is used to manage transactions.</li>
 *     <li>cache: The {@link Map} object that is used to cache entities.</li>
 *     <li>isOpen: A boolean value indicating whether the EntityManager is open or closed.</li>
 * </ul>
 */
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

    /**
     * <p>checks if the EntityManager is open. If the EntityManager is closed, an EntityManagerException is thrown.</p>
     */
    private void validateSession() {
        if (!this.isOpen) {
            throw new EntityManagerException("Entity manager closed", "Need to create new EntityManager instance");
        }
    }

    /**
     * <p> Persists the given entity to the database.</p>
     * <p>This method first validates the session, then checks if the entity is a valid entity using the {@link DaoUtils#isValidEntity} method, and finally calls the {@link JdbcDao#persist} to execute the SQL query</p>
     * @param entity {@link Object} entity to be validated
     */
    public void persist(Object entity) {
        validateSession();
        validatePersistEntity(entity, context.getCache());
        this.jdbcDao.persist(entity);
    }

    @Override
    public <T> T merge(T entity) {
        validateSession();
        return null;
    }

    /**
     * <p>Removes the given entity from the database. This method first validates the session.</p>
     * <p> Then removes entity itself. The entity is also removed from the cache.</p>
     * @param entity {@link Object} - entity to be removed
     */
    @Override
    public void remove(Object entity) {
        validateSession();
        this.jdbcDao.remove(entity);
    }

    /**
     * <p>Finds the entity with the given primary key in the database. This method first validates the session, then gets the table name and a supplier that fetches the entity from the database using the jdbcDao field and the CacheUtils class.</p>
     * <p>The entity is also cached using the cache field. The method returns the entity cast to the given entity class</p>
     * @param entityClass {@link Class} - class of the entity to be found
     * @param primaryKey {@link Object} - identifier value of the given entity
     * @return entity {@link Object} - generated entity form the database row record
     */
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

    /**
     * Flush run dirty checking and update all entities changed during transaction
     */
    @Override
    public void flush() {
        jdbcDao.compareSnapshots();
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

    /**
     * Clear snapshots and cache inside persistence context.
     */
    @Override
    public void clear() {
        context.clear();
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

    /**
     * Clear snapshots and cache inside persistence context and close session.
     */
    @Override
    public void close() {
        context.clear();
        this.isOpen = false;
    }

    /**
     * Return status for current session
     * @return boolean
     */
    @Override
    public boolean isOpen() {
        return isOpen;
    }

    /**
     * <p>Return EntityTransaction</p>
     *
     * @return EntityTransaction interface
     * @throws TransactionException connection could not be obtained
     **/
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
