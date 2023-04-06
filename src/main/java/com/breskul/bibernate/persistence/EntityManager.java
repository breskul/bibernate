package com.breskul.bibernate.persistence;

import javax.sql.DataSource;
import java.util.Map;

/**
 * <h2>This interface provides an implementation of the basic operations that can be performed on entities.</h2>
 * <p>Following fields are present:</p>
 * <ul>
 *     <li>dataSource: The {@link DataSource} object that is used to connect to the database.</li>
 *     <li>jdbcDao: The {@link JdbcDao} object that is used to execute SQL queries and interact with the database.</li>
 *     <li>entityTransaction: The {@link EntityTransactionImpl} object that is used to manage transactions.</li>
 *     <li>cache: The {@link Map} object that is used to cache entities.</li>
 *     <li>isOpen: A boolean value indicating whether the EntityManager is open or closed.</li>
 * </ul>
 */
public interface EntityManager extends AutoCloseable {

    /**
     * Persists the given entity to the database.
     * This method first validates the session, then checks entity existence in cash, and finally calls the {@link JdbcDao#persist} to execute the SQL query
     * @param entity {@link Object} entity to be validated
     */
    void persist(Object entity);

    /**
     * Merge the state of the given entity into the current persistence context.
     * Params:
     * entity – entity instance
     * Returns:
     * the managed instance that the state was merged to
     * Throws:
     * TransactionException – if there is no transaction
     */
    <T> T merge(T entity);

    /**
     * <p>Removes the given entity from the database. This method first validates the session.</p>
     * <p> Then removes entity itself. The entity is also removed from the cache.</p>
     * @param entity {@link Object} - entity to be removed
     */
    void remove(Object entity);

    /**
     * <p>Finds the entity with the given primary key in the database. This method first validates the session, then gets the table name and a supplier that fetches the entity from the database using the jdbcDao field and the CacheUtils class.</p>
     * <p>The entity is also cached using the cache field. The method returns the entity cast to the given entity class</p>
     * @param entityClass {@link Class} - class of the entity to be found
     * @param primaryKey {@link Object} - identifier value of the given entity
     * @return entity {@link Object} - generated entity form the database row record
     */
    <T> T find(Class<T> entityClass, Object primaryKey);

    /**
     * Flush run dirty checking and update all entities changed during transaction
     */
    void flush();

    /**
     * Clear snapshots and cache inside persistence context.
     */
    void clear();

    /**
     * Remove the given entity from the persistence context, causing
     * a managed entity to become detached.
     * @param entity  entity instance
     */
    void detach(Object entity);

    /**
     * Check if the instance is a managed entity instance belonging to the current persistence context.
     * boolean indicating if entity is in persistence context
     * @param entity – entity instance
     * @return boolean indicating if entity is in persistence context
     */
    boolean contains(Object entity);

    /**
     * Clear snapshots and cache inside persistence context and close session.
     */
    void close();

    /**
     * Return status for current session
     * @return boolean
     */
    boolean isOpen();

    EntityTransaction getTransaction();

}
