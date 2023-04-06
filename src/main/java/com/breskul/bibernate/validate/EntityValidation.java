package com.breskul.bibernate.validate;

import com.breskul.bibernate.annotation.Entity;
import com.breskul.bibernate.annotation.Id;
import com.breskul.bibernate.annotation.enums.Strategy;
import com.breskul.bibernate.exception.InternalException;
import com.breskul.bibernate.exception.JdbcDaoException;
import com.breskul.bibernate.persistence.model.EntityKey;

import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.Map;
import java.util.Objects;

import static com.breskul.bibernate.persistence.util.DaoUtils.getIdentifierValue;
import static com.breskul.bibernate.persistence.util.DaoUtils.getStrategy;

public class EntityValidation {
    private EntityValidation() {}

    /**
     * <p>Check if an object is a valid entity and has a valid identifier.</p>
     *
     * @param entity {@link Object} the entity object to check
     * @param cache  {@link Map} the cache of entities to check against for detached entities
     * @throws JdbcDaoException if the entity is not a valid entity, has more than one @Id annotation,
     *                          has no @Id annotation, or is detached and has a manual id
     *                          set with a @GeneratedValue strategy that is not AUTO
     */
    public static <T> void validatePersistEntity(T entity, Map<EntityKey<?>, Object> cache) {
        var type = entity.getClass();

        validateAnnotation(type, Entity.class);

        validIdAnnotation(type);

        Object id = getIdentifierValue(entity);
        var strategy = getStrategy(entity);
        if (!strategy.equals(Strategy.AUTO) && !Objects.isNull(id) && !cache.containsKey(EntityKey.of(entity.getClass(), id))) {
            throw new JdbcDaoException("Detached entity is passed to persist",
                    "Make sure that you don't set id manually when using @GeneratedValue");
        }
    }

    /**
     * Validate fetch entity for persist annotation and default constructor
     * @param entityType type of entity
     */
    public static <T> void validateFetchEntity(Class<T> entityType) {
        validateAnnotation(entityType, Entity.class);
        validIdAnnotation(entityType);
        try {
            entityType.getDeclaredConstructor();
        } catch (NoSuchMethodException e) {
            throw new InternalException("Default constructor does not exist", "Set up default constructor for entity", e);
        }
    }

    private static void validIdAnnotation(Class<?> entityType) {
        long idAnnotationCount = Arrays.stream(entityType.getDeclaredFields()).filter(field -> field.isAnnotationPresent(Id.class)).count();
        if (idAnnotationCount > 1) {
            throw new JdbcDaoException("There are more than one @Id annotation for %s".formatted(entityType.getName()),
                    "Make sure that only one @Id annotation present");
        } else if (idAnnotationCount == 0) {
            throw new JdbcDaoException("There is no @Id annotation for %s".formatted(entityType.getName()),
                    "Make sure that only one @Id annotation present");
        }
    }

    private static <T, A extends Annotation> void validateAnnotation(Class<T> entityType, Class<A> annotationClass) {
        if (Objects.isNull(entityType.getAnnotation(annotationClass))) {
            throw new JdbcDaoException("%s is not a valid entity class".formatted(entityType.getName()),
                    "@Entity annotation should be present");
        }
    }
}
