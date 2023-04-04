package com.breskul.bibernate.persistence.util;

import com.breskul.bibernate.persistence.EntityKey;

import java.util.Map;
import java.util.function.Supplier;

/**
 * <h3>Utils methods for first level cache</h3>
 */
public class CacheUtils {

    /**
     * <p>This method return value from cache if value exist. If value does not exist then value will return from supplier</p>
     *
     * @param entityKey {@link EntityKey} key value for cache
     * @param cache {@link Map}this is map cache
     * @param supplier {@link Supplier} return value if key does not exist inside cache
     * @return return cached value or value from supplier
     **/
    public static <T> T processCache(EntityKey<T> entityKey, Map<EntityKey<?>, Object> cache, Supplier<?> supplier) {
        if (cache.containsKey(entityKey)) {
            return entityKey.entity().cast(cache.get(entityKey));
        } else {
            Object result = supplier.get();
            cache.put(entityKey, result);
            return entityKey.entity().cast(result);
        }
    }
}
