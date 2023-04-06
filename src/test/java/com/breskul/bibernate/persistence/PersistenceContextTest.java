package com.breskul.bibernate.persistence;

import com.breskul.bibernate.persistence.model.EntityKey;
import com.breskul.bibernate.persistence.model.Snapshot;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class PersistenceContextTest {

    @Test
    @DisplayName("Test add to cache")
    public void testAddToCache() {
        PersistenceContext persistenceContext = new PersistenceContext();
        persistenceContext.addToCache("Entity1", 1);
        EntityKey<?> entityKey = EntityKey.of("Entity1".getClass(), 1);
        assertEquals("Entity1", persistenceContext.getCache().get(entityKey), "addToCache test failed!");
    }

    @Test
    @DisplayName("Test remove from cache")
    public void testRemoveFromCache() {
        PersistenceContext persistenceContext = new PersistenceContext();
        persistenceContext.addToCache("Entity2", 2);
        persistenceContext.removeFromCache("Entity2".getClass(), 2);
        EntityKey<?> entityKey = EntityKey.of("Entity2".getClass(), 2);
        assertNull(persistenceContext.getCache().get(entityKey), "removeFromCache test failed!");
    }

    @Test
    @DisplayName("Test add to snapshot")
    public void testAddToSnapshot() {
        PersistenceContext persistenceContext = new PersistenceContext();
        persistenceContext.addToSnapshot("Entity3", 3, "values");
        EntityKey<?> entityKey = EntityKey.of("Entity3".getClass(), 3);
        assertEquals(Snapshot.Status.ACTUAL, persistenceContext.getSnapshots().get(entityKey).getStatus(), "addToSnapshot test failed!");
    }

    @Test
    @DisplayName("Test removeSnapshot")
    public void testRemoveSnapshot() {
        PersistenceContext persistenceContext = new PersistenceContext();
        persistenceContext.addToSnapshot("Entity4", 4, "values");
        persistenceContext.removeSnapshot("Entity4".getClass(), 4);
        EntityKey<?> entityKey = EntityKey.of("Entity4".getClass(), 4);
        assertEquals(Snapshot.Status.REMOVED, persistenceContext.getSnapshots().get(entityKey).getStatus(), "removeSnapshot test failed!");
    }

    @Test
    @DisplayName("Test clear context")
    public void testClear() {
        PersistenceContext persistenceContext = new PersistenceContext();
        persistenceContext.addToCache("Entity5", 5);
        persistenceContext.addToSnapshot("Entity6", 6, "values");
        persistenceContext.clear();
        assertTrue(persistenceContext.getSnapshots().isEmpty() && persistenceContext.getCache().isEmpty(), "clear test failed!");
    }
}
