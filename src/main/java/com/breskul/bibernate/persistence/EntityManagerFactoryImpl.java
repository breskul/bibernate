package com.breskul.bibernate.persistence;

import jakarta.persistence.*;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.metamodel.Metamodel;

import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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
    public EntityManager createEntityManager(Map map) {
        return null;
    }

    @Override
    public EntityManager createEntityManager(SynchronizationType synchronizationType) {
        return null;
    }

    @Override
    public EntityManager createEntityManager(SynchronizationType synchronizationType, Map map) {
        return null;
    }

    @Override
    public CriteriaBuilder getCriteriaBuilder() {
        return null;
    }

    @Override
    public Metamodel getMetamodel() {
        return null;
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

    @Override
    public Map<String, Object> getProperties() {
        return null;
    }

    @Override
    public Cache getCache() {
        return null;
    }

    @Override
    public PersistenceUnitUtil getPersistenceUnitUtil() {
        return null;
    }

    @Override
    public void addNamedQuery(String name, Query query) {

    }

    @Override
    public <T> T unwrap(Class<T> cls) {
        return null;
    }

    @Override
    public <T> void addNamedEntityGraph(String graphName, EntityGraph<T> entityGraph) {

    }
}
