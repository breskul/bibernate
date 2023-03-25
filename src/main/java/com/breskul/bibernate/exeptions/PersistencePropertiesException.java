package com.breskul.bibernate.exeptions;

/**
 * Throws when the 'persistence.properties' file is not found or can't be loaded
 *
 */
public class PersistencePropertiesException extends CommonException {

    private static final String CAN_NOT_FIND_CAUSE = "Unable to find '%s' file";
    private static final String CAN_NOT_FIND_SUGGESTION = "The '%s' file should be in the classpath";

    private static final String CAN_NOT_LOAD_CAUSE = "Unable to load '%s' file";
    private static final String CAN_NOT_LOAD_SUGGESTION = "The '%s' file should be the correct Properties file";

    public PersistencePropertiesException(String propertiesFile) {
        super(String.format(CAN_NOT_FIND_CAUSE, propertiesFile), String.format(CAN_NOT_FIND_SUGGESTION, propertiesFile));
    }

    public PersistencePropertiesException(String propertiesFile, Throwable e) {
        super(String.format(CAN_NOT_LOAD_CAUSE, propertiesFile), String.format(CAN_NOT_LOAD_SUGGESTION, propertiesFile), e);
    }
}
