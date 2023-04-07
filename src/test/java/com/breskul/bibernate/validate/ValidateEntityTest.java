package com.breskul.bibernate.validate;

import com.breskul.bibernate.annotation.Entity;
import com.breskul.bibernate.annotation.GeneratedValue;
import com.breskul.bibernate.annotation.Id;
import com.breskul.bibernate.annotation.enums.Strategy;
import com.breskul.bibernate.exception.InternalException;
import com.breskul.bibernate.exception.JdbcDaoException;
import com.breskul.bibernate.persistence.model.EntityKey;
import com.breskul.bibernate.validate.test_model.TestFetchEntity;
import com.breskul.bibernate.validate.test_model.TestFetchEntityWithoutDefaultConstructor;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class ValidateEntityTest {

    @Test
    @DisplayName("Validate persist entity")
    public void validatePersistEntity() {
        @Entity
        class TestEntity {
            @Id
            private Long id;
        }
        Map<EntityKey<?>, Object> cache = new HashMap<>();

        assertDoesNotThrow(() -> EntityValidation.validatePersistEntity(new TestEntity(), cache));
    }

    @Test
    @DisplayName("Validate persist entity without Entity annotation")
    public void validatePersistEntityWithoutEntityAnnotation() {
        class TestEntity {
            @Id
            private Long id;
        }
        Map<EntityKey<?>, Object> cache = new HashMap<>();

        assertThrows(JdbcDaoException.class, () -> EntityValidation.validatePersistEntity(new TestEntity(), cache));
    }

    @Test
    @DisplayName("Validate persist entity without id")
    public void validatePersistEntityWithoutId() {
        @Entity
        class TestEntity {
            private Long id;
        }
        Map<EntityKey<?>, Object> cache = new HashMap<>();

        assertThrows(JdbcDaoException.class, () -> EntityValidation.validatePersistEntity(new TestEntity(), cache));
    }

    @Test
    @DisplayName("Validate persist entity with multiple id")
    public void validatePersistEntityWithoutMultipleId() {
        @Entity
        class TestEntity {
            @Id
            private Long id;
            @Id
            private Long id2;
        }
        Map<EntityKey<?>, Object> cache = new HashMap<>();

        assertThrows(JdbcDaoException.class, () -> EntityValidation.validatePersistEntity(new TestEntity(), cache));
    }

    @Test
    @DisplayName("Validate persist entity with sequence id")
    public void validatePersistEntityWithSequenceId() {
        class TestEntity {
            @Id
            @GeneratedValue(strategy = Strategy.SEQUENCE)
            private Long id;
        }
        TestEntity testEntity = new TestEntity();
        testEntity.id = 1L;
        EntityKey<?> entityKey = EntityKey.of(testEntity.getClass(), testEntity.id);
        Map<EntityKey<?>, Object> cache = new HashMap<>();
        cache.put(entityKey, testEntity);

        assertThrows(JdbcDaoException.class, () -> EntityValidation.validatePersistEntity(testEntity, cache));
    }

    @Test
    @DisplayName("Validate fetch entity")
    public void validateFetchEntity() {
        assertDoesNotThrow(() -> EntityValidation.validateFetchEntity(TestFetchEntity.class));
    }

    @Test
    @DisplayName("Validate fetch entity with multiple id")
    public void validateFetchEntityWithMultipleId() {
        @Entity
        class TestEntity {
            @Id
            private Long id;
            @Id
            private Long id2;
        }
        assertThrows(JdbcDaoException.class,() -> EntityValidation.validateFetchEntity(TestEntity.class));
    }

    @Test
    @DisplayName("Validate fetch entity without entity annotation")
    public void validateFetchEntityWithoutEntityAnnotation() {
        class TestEntity {
            @Id
            private Long id;
        }
        assertThrows(JdbcDaoException.class,() -> EntityValidation.validateFetchEntity(TestEntity.class));
    }

    @Test
    @DisplayName("Validate fetch entity without default constructor")
    public void validateFetchEntityWithoutDefaultConstructor() {
        assertThrows(InternalException.class,() -> EntityValidation.validateFetchEntity(TestFetchEntityWithoutDefaultConstructor.class));
    }
}
