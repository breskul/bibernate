package com.breskul.bibernate.exeptions;

/**
 * Common exception class<br> Extends the {@link RuntimeException}<br> Each exception class in the
 * project should extend this class
 *
 */
public abstract class CommonException extends RuntimeException {

    public static final String PATTERN = "%s - %s";

    protected CommonException(String cause, String suggestedSolution, Exception e) {
        super(String.format(PATTERN, cause, suggestedSolution), e);
    }
}
