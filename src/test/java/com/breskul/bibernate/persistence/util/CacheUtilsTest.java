package com.breskul.bibernate.persistence.util;

import com.breskul.bibernate.persistence.EntityKey;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class CacheUtilsTest {

    record TestEntity(Long id) {}

    @Test
    @DisplayName("Test processCache get value from cache")
    public void getValueFromCache() {
        TestEntity testEntity = new TestEntity(1L);
        EntityKey<?> entityKey = EntityKey.of(TestEntity.class, testEntity.id);

        Map<EntityKey<?>, Object> cache = new HashMap<>();
        cache.put(entityKey, testEntity);

        TestEntity result = (TestEntity) CacheUtils.processCache(entityKey, cache, () -> new TestEntity(2L));
        assertEquals(testEntity, result);
    }

    @Test
    @DisplayName("Test processCache get value from supplier")
    public void getValueFromSupplier() {
        TestEntity testEntity = new TestEntity(1L);
        EntityKey<?> entityKey = EntityKey.of(TestEntity.class, testEntity.id);

        Map<EntityKey<?>, Object> cache = new HashMap<>();

        TestEntity result = (TestEntity) CacheUtils.processCache(entityKey, cache, () -> testEntity);
        assertEquals(testEntity, result);
    }
}
