package com.breskul.bibernate.repository;

import com.breskul.bibernate.configuration.PersistenceProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class DataSourceFactoryTest {

    @BeforeEach
    public void init(){
        PersistenceProperties.initialize();
    }

    @Test
    public void createDataSourceFactory() {
        DataSourceFactory dataSourceFactory = DataSourceFactory.getInstance();
        assertNotNull(dataSourceFactory);
    }

    @Test
    public void dataSourceFactoryIsSingleton() {
        DataSourceFactory dataSourceFactory1 = DataSourceFactory.getInstance();
        DataSourceFactory dataSourceFactory2 = DataSourceFactory.getInstance();
        assertEquals(dataSourceFactory1.getClass(), dataSourceFactory2.getClass());
    }

    @Test
    public void getDataSource() {
        DataSourceFactory dataSourceFactory = DataSourceFactory.getInstance();
        DataSource dataSource = dataSourceFactory.getDataSource();
        assertNotNull(dataSource);
    }
}
