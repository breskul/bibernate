package com.breskul.bibernate.persistence;

import com.breskul.bibernate.persistence.model.EntityKey;
import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

@Getter
public class PersistenceContext {

    private final Map<EntityKey<?>, Object> cache;
    private final Map<EntityKey<?>, String> snapshots;

    public PersistenceContext() {
        this.cache = new HashMap<>();
        this.snapshots = new HashMap<>();
    }

    public void addToCache(Object entityType, Object key) {
        var entityKey = EntityKey.of(entityType.getClass(), key);
        cache.put(entityKey, entityType);
    }

    public void removeFromCache(Class<?> entityType, Object key) {
        var entityKey = EntityKey.of(entityType, key);
        cache.remove(entityKey);
    }

    public void addToSnapshot(Object entityType, Object key, String values) {
        var entityKey = EntityKey.of(entityType.getClass(), key);
        snapshots.put(entityKey, values);
    }

    public void removeFromSnapshot(Class<?> entityType, Object key) {
        var entityKey = EntityKey.of(entityType, key);
        snapshots.remove(entityKey);
    }

    public void clear() {
        snapshots.clear();
        cache.clear();
    }
}
