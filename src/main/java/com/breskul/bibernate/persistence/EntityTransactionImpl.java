package com.breskul.bibernate.persistence;

import com.breskul.bibernate.exception.TransactionException;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

public class EntityTransactionImpl implements EntityTransaction {

    private final PersistenceContext context;
    private final DataSource dataSource;
    private final JdbcDao jdbcDao;
    private Connection connection;
    private boolean isActive;
    private boolean isRollbackOnly;

    public EntityTransactionImpl(DataSource dataSource, JdbcDao jdbcDao, PersistenceContext context) {
        this.dataSource = dataSource;
        this.jdbcDao = jdbcDao;
        this.context = context;
    }

    @Override
    public void begin() {
        if (isActive()) {
            closeConnection();
            isActive = false;
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
                jdbcDao.compareSnapshots();
                connection.commit();
                closeConnection();
            } catch (SQLException exception) {
                throw new TransactionException("Can not commit transaction", "Check db server health", exception);
            }
        }
        this.isActive = false;
    }

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
            context.clear();
        } catch (SQLException exception) {
            throw new TransactionException("Can not rollback transaction", "Check db server health", exception);
        }

        this.isActive = false;
    }

    @Override
    public void setRollbackOnly() {
        if (!isActive()) {
            throw new TransactionException(
                    "Transaction have been not opened",
                    "Before set rollback only transaction should be open");
        }
        this.isRollbackOnly = true;
    }

    @Override
    public boolean getRollbackOnly() {
        if (!isActive()) {
            throw new TransactionException(
                    "Transaction have been not opened",
                    "Before get rollback only transaction should be open");
        }
        return isRollbackOnly;
    }

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
