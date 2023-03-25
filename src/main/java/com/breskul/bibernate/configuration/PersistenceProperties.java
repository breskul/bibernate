package com.breskul.bibernate.configuration;

import com.breskul.bibernate.exeptions.InitializePersistencePropertiesException;
import com.breskul.bibernate.exeptions.PersistencePropertiesException;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Configuration class for load and store configuration properties from the classpath
 */
public class PersistenceProperties {
    private static final String DEFAULT_PROPERTIES_FILE = "persistence.properties";

    private Properties properties;

    private static PersistenceProperties instance;

    public static synchronized PersistenceProperties getInstance() {
        if (instance == null) {
            throw new InitializePersistencePropertiesException();
        }
        return instance;
    }

    private PersistenceProperties(String propertiesFile) {
        loadProperties(propertiesFile);
    }

    /**
     * Initialize PersistenceProperties with default 'persistence.properties' properties file
     */
    public static void initialize(){
        initialize(DEFAULT_PROPERTIES_FILE);
    }

    /**
     * Initialize PersistenceProperties with default properties file
     * @param propertiesFile custom properties file name
     */
    public static synchronized void initialize(String propertiesFile){
        if (instance == null) {
            instance = new PersistenceProperties(propertiesFile);
        }
    }

    private void loadProperties(String propertiesFile) {
        try (InputStream input = this.getClass().getClassLoader().getResourceAsStream(propertiesFile)) {
            properties = new Properties();

            if (input == null) {
                throw new PersistencePropertiesException(propertiesFile);
            }

            properties.load(input);

        } catch (IOException e) {
            throw new PersistencePropertiesException(propertiesFile, e);
        }
    }

    /**
     * Return configuration property value by property key
     *
     * @param name property key
     * @return property value
     */
    public String getProperty(String name) {
        return properties.getProperty(name);
    }
}


