package com.breskul.demo;

import com.breskul.bibernate.configuration.PersistenceProperties;
import com.breskul.bibernate.persistence.EntityManagerFactoryImpl;
import com.breskul.bibernate.repository.DataSourceFactory;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;

import javax.sql.DataSource;

public class Main {
    public static void main(String[] args) {
        PersistenceProperties.initialize();
        DataSource dataSource = DataSourceFactory.getInstance().getDataSource();

        EntityManagerFactory entityManagerFactory = new EntityManagerFactoryImpl(dataSource);
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        entityManagerFactory.close();
    }
}
