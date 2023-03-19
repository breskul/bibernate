package com.breskul.bibernate.repository;

import com.breskul.bibernate.configuration.PersistenceProperties;
import com.zaxxer.hikari.HikariDataSource;

import javax.sql.DataSource;

/**
 * Class that produces the pooled hikari datasource
 */
public class DataSourceFactory {
    private final DataSource dataSource;

    private static DataSourceFactory instance;

    public static synchronized DataSourceFactory getInstance() {
        if (instance == null) {
            instance = new DataSourceFactory();
        }
        return instance;
    }

    private DataSourceFactory() {
        dataSource = configureDataSource();
    }

    /**
     * The method that return configured datasource
     *
     * @return datasource
     */
    public DataSource getDataSource() {
        return dataSource;
    }

    private DataSource configureDataSource() {
        PersistenceProperties properties = PersistenceProperties.getInstance();
        HikariDataSource hikariDataSource = new HikariDataSource();
        hikariDataSource.setJdbcUrl(properties.getProperty("db.url"));
        hikariDataSource.setUsername(properties.getProperty("db.user"));
        hikariDataSource.setPassword(properties.getProperty("db.password"));

        return hikariDataSource;
    }
}
