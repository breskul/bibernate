package com.breskul.bibernate.configuration;

import com.breskul.bibernate.exeptions.PersistencePropertiesException;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class PersistenceProperties {
    private static final String PROPERTIES_FILE = "persistence.properties";

    private Properties properties;

    private static PersistenceProperties instance;

    public static synchronized PersistenceProperties getInstance() {
        if (instance == null) {
            instance = new PersistenceProperties();
        }
        return instance;
    }

    private PersistenceProperties() {
        loadProperties();
    }

    private void loadProperties() {
        try (InputStream input = this.getClass().getClassLoader().getResourceAsStream(PROPERTIES_FILE)) {
            properties = new Properties();

            if (input == null) {
                throw new PersistencePropertiesException("Unable to find 'persistence.properties' file");
            }

            properties.load(input);

        } catch (IOException e) {
            throw new PersistencePropertiesException("Unable to load 'persistence.properties' file", e);
        }
    }

    public String getProperty(String name) {
        return properties.getProperty(name);
    }
}


