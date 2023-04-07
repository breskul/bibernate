package com.breskul.bibernate.persistence.model;

/**
 * EntityKey record using for cache key
 * @param <T> type of object for saved to cache value
 */
public record EntityKey<T>(Class<T> entity, Object id) {

    /**
     * @param entity class for cache value
     * @param id for cache value
     * @return return EntityKey instance
     **/
    public static EntityKey<?> of(Class<?> entity, Object id) {
        return new EntityKey<>(entity, id);
    }
}
