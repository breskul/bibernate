package com.breskul.bibernate.persistence;

import com.breskul.bibernate.exception.TransactionException;

/**
 * Interface used to control transactions on resource-local entity managers.
 * The EntityManager.getTransaction() method returns the EntityTransaction interface.
 */
public interface EntityTransaction {

    /**
     * Start transaction.
     * @throws TransactionException if transaction have been already opened or can not open transaction
     **/
    void begin();

    /**
     * Commit transaction.
     * @throws TransactionException if transaction have not been already opened or can not commit transaction
     **/
    void commit();

    /**
     * Rollback transaction.
     * @throws TransactionException if transaction have not been already opened or can not rollback transaction
     **/
    void rollback();

    /**
     * Set rollback only mode.
     * @throws TransactionException if transaction have not been already opened
     **/
    void setRollbackOnly();

    /**
     * Get rollback only mode.
     * @throws TransactionException if transaction have not been already opened
     * @return return boolean value for indication mode
     **/
    boolean getRollbackOnly();

    /**
     * Check transaction status
     * @return return boolean value for transaction status
     **/
    boolean isActive();
}
