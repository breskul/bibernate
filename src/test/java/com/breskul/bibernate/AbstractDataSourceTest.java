package com.breskul.bibernate;

import com.breskul.bibernate.configuration.PersistenceProperties;
import com.breskul.bibernate.repository.DataSourceFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.TestInstance;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.function.Consumer;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public abstract class AbstractDataSourceTest {

    public static final String CLEAN_PERSON_TABLE = "DELETE FROM users";
    public static final String CLEAN_NOTE_TABLE = "DELETE FROM notes";

    protected DataSource dataSource;

    @BeforeAll
    protected void init() {
        PersistenceProperties.initialize();
        DataSourceFactory dataSourceFactory = DataSourceFactory.getInstance();
        dataSource = dataSourceFactory.getDataSource();
    }

    public void doInConnection(Consumer<Connection> consumer) {
        try (Connection connection = dataSource.getConnection()) {
            consumer.accept(connection);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }


}
