package com.breskul.bibernate.exception;

/**
 * TransactionException process errors depends from work with transactions
 */
public class TransactionException extends CommonException {

    public TransactionException(String cause, String suggestedSolution) {
        super(cause, suggestedSolution);
    }

    public TransactionException(String cause, String suggestedSolution, Throwable e) {
        super(cause, suggestedSolution, e);
    }
}
