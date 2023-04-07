package com.breskul.bibernate.configuration;

import com.breskul.bibernate.exception.InitializePersistencePropertiesException;
import com.breskul.bibernate.exception.PersistencePropertiesException;
import org.junit.jupiter.api.*;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.*;

class PersistencePropertiesTest {
    @AfterEach
    void nullifyInstance() throws NoSuchFieldException, IllegalAccessException {
        PersistenceProperties.initialize();
        PersistenceProperties persistenceProperties = PersistenceProperties.getInstance();
        Field instanceField = persistenceProperties.getClass().getDeclaredField("instance");
        instanceField.setAccessible(true);
        instanceField.set(persistenceProperties, null);
    }

    @Test
    @DisplayName("Test getProperty")
    void testGetProperty() {
        PersistenceProperties.initialize();
        PersistenceProperties persistenceProperties = PersistenceProperties.getInstance();
        String value = persistenceProperties.getProperty("db.url");
        assertEquals("jdbc:h2:mem:test;DB_CLOSE_DELAY=-1;INIT=runscript from 'classpath:/sql/init.sql'", value);
    }

    @Test
    @DisplayName("Test initialize with custom file")
    void testInitializeCustomFile() {
        PersistenceProperties.initialize("custom.properties");
        PersistenceProperties persistenceProperties = PersistenceProperties.getInstance();

        String value = persistenceProperties.getProperty("db.url");
        assertEquals("jdbc:postgresql://localhost:5432/postgres", value);
    }

    @Test
    @DisplayName("Test load properties without initialization")
    void testLoadPropertiesWithoutInitialize() {

        assertThrows(InitializePersistencePropertiesException.class,
                PersistenceProperties::getInstance);
    }

    @Test
    @DisplayName("Test load properties not found")
    void testLoadPropertiesNotFound() {
        assertThrows(PersistencePropertiesException.class,
                () -> PersistenceProperties.initialize("nonexistent.properties"));
    }

    @Test
    @DisplayName("Test load properties IO exception")
    void testLoadPropertiesIOException() {
        assertThrows(PersistencePropertiesException.class,
                () -> PersistenceProperties.initialize("invalid.properties"));
    }
}
