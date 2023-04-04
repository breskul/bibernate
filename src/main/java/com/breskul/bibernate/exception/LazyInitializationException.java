package com.breskul.bibernate.exception;

/**
 * LazyInitializationException throws when try to get resource out of the session
 */
public class LazyInitializationException extends CommonException {
    private static final String CAUSE = "Couldn't load resources because the session has been already closed";
    private static final String SUGGESTED_SOLUTION = "Check that session is created and create the session";

    public LazyInitializationException(Throwable e) {
        super(CAUSE, SUGGESTED_SOLUTION, e);
    }
}
