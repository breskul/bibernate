package com.breskul.bibernate.exception;

public class JdbcDaoException extends CommonException {

    private static final String CHECK_YOUR_SQL_QUERY = "check your sql query";

    public JdbcDaoException(String cause, String suggestedSolution, Throwable e) {
        super(cause, suggestedSolution, e);
    }

    public JdbcDaoException(String cause, String suggestedSolution) {
        super(cause, suggestedSolution);
    }

    public JdbcDaoException(String cause) {
        super(cause, CHECK_YOUR_SQL_QUERY);
    }

    public JdbcDaoException(String cause, Throwable e) {
        super(cause, CHECK_YOUR_SQL_QUERY, e);
    }
}
