package com.breskul.bibernate.persistence.util;

import com.breskul.bibernate.annotation.*;
import com.breskul.bibernate.exception.DaoUtilsException;
import com.breskul.bibernate.exception.InternalException;
import com.breskul.bibernate.exception.JdbcDaoException;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Optional;

/**
 * Reflection utils methods
 */
public class DaoUtils {
    private DaoUtils() {
    }

    public static Field getIdentifierField(Class<?> entityClass) {
        var cause = "entity is not marked with Id annotation";
        var solution = "mark id column with Id annotation";
        return Arrays.stream(entityClass.getDeclaredFields())
                .filter(field -> field.isAnnotationPresent(Id.class))
                .findAny()
                .orElseThrow(() -> new DaoUtilsException(cause, solution));
    }

    public static String getIdentifierFieldName(Class<?> entityClass) {
        Field field = getIdentifierField(entityClass);
        return field.getName();
    }

    public static String getColumnName(Field field) {
        return Optional.ofNullable(field.getAnnotation(Column.class)).map(Column::name).orElse(field.getName());
    }

    public static <T> Object getIdentifierValue(T entity) {
        Field identifierField = getIdentifierField(entity.getClass());
        try {
            identifierField.setAccessible(true);
            return identifierField.get(entity);
        } catch (IllegalAccessException e) {
            var cause = "entity does not have id field value";
            var solution = "make sure that entity has id field value";
            throw new DaoUtilsException(cause, solution);
        }

    }

    public static String getClassTableName(Class<?> entityClass) {
        return Optional.ofNullable(entityClass.getAnnotation(Table.class))
                .map(Table::name)
                .orElse(entityClass.getSimpleName());

    }

    public static <T> void isValidEntity(Class<T> type) {
        if (type.isAnnotationPresent(Entity.class)) {
            long idAnnotationCount = Arrays.stream(type.getDeclaredFields()).filter(field -> field.isAnnotationPresent(Id.class)).count();
            if (idAnnotationCount > 1) {
                throw new JdbcDaoException("There are more than one @Id annotation for %s".formatted(type.getName()), "Make sure that only one @Id annotation present");
            } else if (idAnnotationCount == 0) {
                throw new JdbcDaoException("There is no @Id annotation for %s".formatted(type.getName()), "Make sure that only one @Id annotation present");
            }
        } else {
            throw new JdbcDaoException("%s is not a valid entity class".formatted(type.getName()), "@Entity annotation should be present");
        }
    }

    public static String resolveFieldName(Field field) {
        if (field.isAnnotationPresent(Column.class)) {
            String fieldName = field.getAnnotation(Column.class).name();
            if (fieldName != null && !fieldName.isEmpty()) {
                return fieldName;
            }
        } else if (field.isAnnotationPresent(JoinColumn.class)){
            String fieldName = field.getAnnotation(JoinColumn.class).name();
            if (fieldName != null && !fieldName.isEmpty()) {
                return fieldName;
            }
        }
        return field.getName();
    }

    public static Object getFieldValue(Object object, Field idField) {
        try {
            Field declaredField = object.getClass().getDeclaredField(idField.getName());
            declaredField.setAccessible(true);
            return declaredField.get(object);
        } catch (IllegalAccessException | NoSuchFieldException e) {
            throw new InternalException("Can't get field %s".formatted(idField.getName()), "", e);
        }
    }

    public static Field getFieldByAnnotation(Class<?> object, Class<? extends Annotation> annotation) {
        return Arrays.stream(object.getDeclaredFields()).filter(field -> field.isAnnotationPresent(annotation)).findAny().orElseThrow(() -> new RuntimeException(annotation.getName() + " is not present"));
    }

    public static boolean isRegularField(Field field) {
        return !isEntityField(field) && !isEntityCollectionField(field);
    }

    public static boolean isEntityField(Field field) {
        return field.isAnnotationPresent(ManyToOne.class);
    }

    /**
     * This method set value to field mapped by accepted annotation
     * @param entity
     * @param value
     * @param annotationClass
     **/
    public static void setValueToField(Object entity, Object value, Class<? extends Annotation> annotationClass) {
        var entityType = entity.getClass();
        Field field = Arrays.stream(entityType.getDeclaredFields())
                .filter(f -> f.isAnnotationPresent(annotationClass))
                .findFirst()
                .orElseThrow(() -> {
                    var cause = "Can not find field with annotation name: " + annotationClass.getSimpleName();
                    var solution = "Apply %s annotation to entity %s"
                            .formatted(annotationClass.getSimpleName(), entityType.getSimpleName());
                    return new InternalException(cause, solution);
                });
        setValueToField(entity, value, field);
    }

    /**
     * Set value to the entity field
     * @param entity
     * @param value
     * @param field
     */
    public static void setValueToField(Object entity, Object value, Field field) {
        try {
            field.setAccessible(true);
            field.set(entity, value);
        } catch (IllegalAccessException e) {
            var cause = "Can not set value to field";
            var solution = "Check correctness type for injected value to field";
            throw new InternalException(cause, solution, e);
        }
    }

    public static boolean isEntityCollectionField(Field field) {
        return field.isAnnotationPresent(OneToMany.class);
    }

}
