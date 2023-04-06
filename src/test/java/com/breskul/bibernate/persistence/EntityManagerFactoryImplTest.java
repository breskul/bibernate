package com.breskul.bibernate.persistence;

import static org.junit.jupiter.api.Assertions.*;

import javax.sql.DataSource;

import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class EntityManagerFactoryImplTest {
    private DataSource dataSource;

    private EntityManagerFactoryImpl entityManagerFactory;

    @BeforeEach
    public void setUp() throws Exception {
        entityManagerFactory = new EntityManagerFactoryImpl(dataSource);
    }

    @AfterEach
    public void tearDown() throws Exception {
        entityManagerFactory.close();
    }

    @Test
    public void testCreateEntityManager() {
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        assertNotNull(entityManager);
        assertTrue(entityManager instanceof EntityManagerImpl);
    }

    @Test
    public void testIsOpen() {
        assertTrue(entityManagerFactory.isOpen());
        entityManagerFactory.close();
        assertFalse(entityManagerFactory.isOpen());
    }

    @Test
    public void testClose() {
        EntityManager entityManager1 = entityManagerFactory.createEntityManager();
        EntityManager entityManager2 = entityManagerFactory.createEntityManager();
        entityManagerFactory.close();
        assertFalse(entityManagerFactory.isOpen());
        assertFalse(entityManager1.isOpen());
        assertFalse(entityManager2.isOpen());
    }

}
