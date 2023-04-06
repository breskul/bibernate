package com.breskul.bibernate.persistence;

import com.breskul.bibernate.persistence.model.EntityKey;
import com.breskul.bibernate.persistence.model.Snapshot;
import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.Map;


/**
 * Context contain cache and snapshots for session and provide methods for them.
 */
@Getter
@Setter
public class PersistenceContext {

    private final Map<EntityKey<?>, Object> cache;
    private final Map<EntityKey<?>, Snapshot> snapshots;

    public PersistenceContext() {
        this.cache = new HashMap<>();
        this.snapshots = new HashMap<>();
    }

    /**
     * Add new entity to cache
     * @param entityType new entity for cache
     * @param key unique key for entity
     */
    public void addToCache(Object entityType, Object key) {
        var entityKey = EntityKey.of(entityType.getClass(), key);
        cache.put(entityKey, entityType);
    }

    /**
     * Remove entity from cache
     * @param entityType type for removing entity
     * @param key unique key for entity
     */
    public void removeFromCache(Class<?> entityType, Object key) {
        var entityKey = EntityKey.of(entityType, key);
        cache.remove(entityKey);
    }

    /**
     * Add new snapshot for entity
     * @param entityType type entity for snapshot
     * @param key unique key for entity
     * @param values snapshot value for entity
     */
    public void addToSnapshot(Object entityType, Object key, String values) {
        var entityKey = EntityKey.of(entityType.getClass(), key);
        var snapshot = new Snapshot(values, Snapshot.Status.ACTUAL);
        snapshots.put(entityKey, snapshot);
    }

    /**
     * Update snapshot status for REMOVED
     * @param entityType type entity for snapshot
     * @param key unique key for entity
     */
    public void removeSnapshot(Class<?> entityType, Object key) {
        var entityKey = EntityKey.of(entityType, key);
        snapshots.get(entityKey).setStatus(Snapshot.Status.REMOVED);
    }

    /**
     * Clear snapshots and cache
     */
    public void clear() {
        snapshots.clear();
        cache.clear();
    }
}
