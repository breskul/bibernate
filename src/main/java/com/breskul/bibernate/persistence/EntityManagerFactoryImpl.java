package com.breskul.bibernate.persistence;

import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.List;

public class EntityManagerFactoryImpl implements EntityManagerFactory {
    private final DataSource dataSource;
    private final List<EntityManager> entityManagers = new ArrayList<>();

    private boolean isOpen;

    public EntityManagerFactoryImpl(DataSource dataSource) {
        this.dataSource = dataSource;
        this.isOpen = true;
    }

    @Override
    public EntityManager createEntityManager() {
        EntityManager entityManager = new EntityManagerImpl(dataSource);
        entityManagers.add(entityManager);
        return entityManager;
    }

    @Override
    public boolean isOpen() {
        return isOpen;
    }

    @Override
    public void close() {
        entityManagers.forEach(EntityManager::close);
        entityManagers.clear();
        this.isOpen = false;
    }
}
