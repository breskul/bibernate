package com.breskul.bibernate.exeptions;

/**
 * Throws when the 'persistence.properties' file is not found or can't be loaded
 *
 */
public class PersistencePropertiesException extends CommonException {

    private static final String CANT_FIND_CAUSE = "Unable to find '%s' file";
    private static final String CANT_FIND_SUGGESTION = "The 'persistence.properties' file should be in the classpath";

    private static final String CANT_LOAD_CAUSE = "Unable to load '%s' file";
    private static final String CANT_LOAD_SUGGESTION = "The 'persistence.properties' file should be the correct Properties file";

    public PersistencePropertiesException(String propertiesFile) {
        super(String.format(CANT_FIND_CAUSE, propertiesFile), CANT_FIND_SUGGESTION);
    }

    public PersistencePropertiesException(String propertiesFile, Throwable e) {
        super(String.format(CANT_LOAD_CAUSE, propertiesFile), CANT_LOAD_SUGGESTION, e);
    }
}
