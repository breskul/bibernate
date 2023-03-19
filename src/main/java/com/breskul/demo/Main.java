package com.breskul.demo;

import com.breskul.bibernate.repository.DataSourceFactory;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;

import javax.sql.DataSource;

public class Main {
    public static void main(String[] args) {
        DataSource dataSource = DataSourceFactory.getInstance().getDataSource();

        EntityManagerFactory entityManagerFactory = Persistence.createEntityManagerFactory("demo");
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        entityManagerFactory.close();
    }
}
