package com.breskul.bibernate.exeptions;

public class JdbcDaoException extends CommonException{
    public JdbcDaoException(String cause, String suggestedSolution, Throwable e) {
        super(cause, suggestedSolution, e);
    }
    public JdbcDaoException(String cause, String suggestedSolution) {
        super(cause, suggestedSolution);
    }
}
