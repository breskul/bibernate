package com.breskul.bibernate.persistence;

import com.breskul.bibernate.exception.TransactionException;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * Interface used to control transactions on resource-local entity
 * managers. The {@link EntityManager#getTransaction
 * EntityManager.getTransaction()} method returns the
 * <code>EntityTransaction</code> interface.
 */
public class EntityTransactionImpl implements EntityTransaction {

    private final DataSource dataSource;
    private final JdbcDao jdbcDao;
    private Connection connection;
    private boolean isActive;
    private boolean isRollbackOnly;

    public EntityTransactionImpl(DataSource dataSource, JdbcDao jdbcDao) {
        this.dataSource = dataSource;
        this.jdbcDao = jdbcDao;
    }

    /**
     * Start transaction.
     * @throws TransactionException if transaction have been already opened or can not open transaction
     **/
    @Override
    public void begin() {
        if (isActive()) {
            closeConnection();
            throw new TransactionException("Transaction have been already opened", "Can be open only one transaction");
        }
        try {
            openConnection();
            connection.setAutoCommit(false);
            isActive = true;
        } catch (SQLException exception) {
            throw new TransactionException("Can not begin transaction", "Check db server health", exception);
        }
    }

    /**
     * Commit transaction.
     * @throws TransactionException if transaction have not been already opened or can not commit transaction
     **/
    @Override
    public void commit() {
        if (!isActive()) {
            throw new TransactionException(
                    "Transaction have been not opened",
                    "Before commit transaction should be open");
        }
        if (getRollbackOnly()) {
            rollback();
        } else {
            try {
                connection.commit();
                closeConnection();
            } catch (SQLException exception) {
                throw new TransactionException("Can not commit transaction", "Check db server health", exception);
            }
        }
        this.isActive = false;
    }

    /**
     * Rollback transaction.
     * @throws TransactionException if transaction have not been already opened or can not rollback transaction
     **/
    @Override
    public void rollback() {
        if (!isActive()) {
            throw new TransactionException(
                    "Transaction have been not opened",
                    "Before rollback transaction should be open");
        }
        try {
            connection.rollback();
            closeConnection();
        } catch (SQLException exception) {
            throw new TransactionException("Can not rollback transaction", "Check db server health", exception);
        }

        this.isActive = false;
    }

    /**
     * Set rollback only mode.
     * @throws TransactionException if transaction have not been already opened
     **/
    @Override
    public void setRollbackOnly() {
        if (!isActive()) {
            throw new TransactionException(
                    "Transaction have been not opened",
                    "Before set rollback only transaction should be open");
        }
        this.isRollbackOnly = true;
    }

    /**
     * Get rollback only mode.
     * @throws TransactionException if transaction have not been already opened
     * @return return boolean value for indication mode
     **/
    @Override
    public boolean getRollbackOnly() {
        if (!isActive()) {
            throw new TransactionException(
                    "Transaction have been not opened",
                    "Before get rollback only transaction should be open");
        }
        return isRollbackOnly;
    }

    /**
     * Check transaction status
     * @return return boolean value for transaction status
     **/
    @Override
    public boolean isActive() {
        return this.isActive;
    }

    private void openConnection() throws SQLException {
        this.connection = dataSource.getConnection();
        this.jdbcDao.setConnection(connection);
    }

    private void closeConnection() {
        try {
            this.connection.close();
        } catch (SQLException exception) {
            throw new TransactionException("Cannot close connection", "Check db server health", exception);
        }
        this.jdbcDao.setConnection(null);
    }
}
