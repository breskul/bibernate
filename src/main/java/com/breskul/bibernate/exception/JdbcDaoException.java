package com.breskul.bibernate.exception;

public class JdbcDaoException extends CommonException {
    public JdbcDaoException(String cause, String suggestedSolution, Throwable e) {
        super(cause, suggestedSolution, e);
    }

    public JdbcDaoException(String cause, String suggestedSolution) {
        super(cause, suggestedSolution);
    }
}
