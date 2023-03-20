package com.breskul.bibernate;

import com.breskul.bibernate.repository.DataSourceFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.TestInstance;

import javax.sql.DataSource;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public abstract class AbstractDataSourceTest {

    protected DataSource dataSource;

    @BeforeAll
    protected void init() {
        DataSourceFactory dataSourceFactory = DataSourceFactory.getInstance();
        dataSource = dataSourceFactory.getDataSource();
    }
}
