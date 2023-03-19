package com.breskul.bibernate.exeptions;

/**
 * Throws when the 'persistence.properties' file is not found or can't be loaded
 *
 */
public class PersistencePropertiesException extends CommonException {
    private static final String CANT_FIND_SUGGESTION = "The 'persistence.properties' file should be in the classpath";

    private static final String CANT_LOAD_SUGGESTION = "The 'persistence.properties' file should be the correct Properties file";

    public PersistencePropertiesException(String cause) {
        super(cause, CANT_FIND_SUGGESTION);
    }

    public PersistencePropertiesException(String cause, Throwable e) {
        super(cause, CANT_LOAD_SUGGESTION, e);
    }
}
