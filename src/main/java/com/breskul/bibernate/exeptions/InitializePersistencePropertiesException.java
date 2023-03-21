package com.breskul.bibernate.exeptions;

public class InitializePersistencePropertiesException extends CommonException {

    private static final String INITIALIZE_CAUSE = "The PersistenceProperties not initialized";
    private static final String INITIALIZE_SUGGESTION = "Use initialize() method for initialization";
    public InitializePersistencePropertiesException() {
        super(INITIALIZE_CAUSE, INITIALIZE_SUGGESTION);
    }
}
