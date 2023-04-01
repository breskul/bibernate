package com.breskul.bibernate.exception;

/**
 * Exception throws by an invoked method or constructor
 *
 * @author Artem Yankovets
 */
public class ReflectionException extends CommonException {

    public ReflectionException(String cause, String suggestedSolution, Throwable e) {
        super(cause, suggestedSolution, e);
    }

    public ReflectionException(String cause, String suggestedSolution) {
        super(cause, suggestedSolution);
    }
}
