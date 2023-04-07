package com.breskul.bibernate;

import com.breskul.bibernate.configuration.PersistenceProperties;
import com.breskul.bibernate.persistence.EntityManager;
import com.breskul.bibernate.persistence.EntityManagerImpl;
import com.breskul.bibernate.persistence.EntityTransaction;
import com.breskul.bibernate.repository.DataSourceFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.TestInstance;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.function.Consumer;
import java.util.function.Function;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public abstract class AbstractDataSourceTest {

    public static final String CLEAN_PERSON_TABLE = "DELETE FROM users";
    public static final String CLEAN_NOTE_TABLE = "DELETE FROM notes";

    public static final String CLEAN_PROFILES_TABLE = "DELETE FROM profiles";
    public static final String CLEAN_COMPANY_TABLE = "DELETE FROM companies";
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

    public void doInLocalEntityManager(Consumer<EntityManager> entityManagerConsumer) {
        doInLocalEntityManagerReturning(em -> {
            entityManagerConsumer.accept(em);
            return null;
        });
    }

    public <T> T doInLocalEntityManagerReturning(Function<EntityManager, T> entityManagerTFunction) {
        EntityManager localEntityManager = new EntityManagerImpl(dataSource);
        EntityTransaction transaction = localEntityManager.getTransaction();
        transaction.begin();
        T result;
        try {

            result = entityManagerTFunction.apply(localEntityManager);
            transaction.commit();

        } catch (Exception e) {
            transaction.rollback();
            throw new RuntimeException(e);
        } finally {
            localEntityManager.close();
        }
        return result;
    }

}
