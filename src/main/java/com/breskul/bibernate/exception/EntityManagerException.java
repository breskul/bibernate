package com.breskul.bibernate.exception;

/**
 * Process inner EntityManager exceptions
 */
public class EntityManagerException extends CommonException {

    public EntityManagerException(String cause, String suggestedSolution) {
        super(cause, suggestedSolution);
    }
}
