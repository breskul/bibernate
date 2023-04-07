package com.breskul.bibernate.persistence.util;

import com.breskul.bibernate.annotation.*;
import com.breskul.bibernate.annotation.enums.CascadeType;
import com.breskul.bibernate.annotation.enums.FetchType;
import com.breskul.bibernate.annotation.enums.Strategy;
import com.breskul.bibernate.collection.LazyList;
import com.breskul.bibernate.exception.InternalException;
import com.breskul.bibernate.exception.JdbcDaoException;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.ParameterizedType;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * {@link DaoUtils} provides reflection utility methods to work with Java Persistence API (JPA) entities.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class DaoUtils {

    /**
     * <p>This method returns a comma-separated list of the names of all the columns of the database table
     * corresponding to a given JPA entity that do not correspond to collection or primary key fields.</p>
     *
     * @param entity {@link Object} the JPA entity for which the list of column names should be returned.
     * @return {@link String} A comma-separated string containing the names of all the columns of the database table
     * corresponding to the given JPA entity that do not correspond to collection or primary key fields.
     */
    public static String getSqlFieldNamesWithoutId(Object entity) {
        var entityClass = entity.getClass();
        return Arrays.stream(entityClass.getDeclaredFields())
                .filter(field -> !isCollectionField(field) && !isIdField(field))
                .map(DaoUtils::resolveColumnName)
                .collect(Collectors.joining(","));
    }

    /**
     * <p>This method returns {@link Boolean#TRUE} if a given field of a JPA entity is annotated with {@link OneToMany},
     * indicating that it corresponds to a collection of related entities.</p>
     *
     * @param field {@link Field} the field to check.
     * @return {@link Boolean}
     */
    public static boolean isCollectionField(Field field) {
        return field.isAnnotationPresent(OneToMany.class);
    }

    /**
     * <p>This method returns {@link Boolean#TRUE}  if a given field of a JPA entity is annotated with {@link Id},
     * indicating that it corresponds to the primary key of the entity.</p>
     *
     * @param field {@link Field} the field to check.
     * @return {@link Boolean}
     */
    public static boolean isIdField(Field field) {
        return field.isAnnotationPresent(Id.class);
    }

    /**
     * <p>This method returns the name of the database column corresponding to a given field of a JPA entity.
     * If the field is annotated with {@link JoinColumn}, the name specified in that annotation is used.
     * Otherwise, the name of the field itself is used.</p>
     *
     * @param field {@link Field} the field for which to return the database column name..
     * @return {@link String} the name of the database column corresponding to the given field.
     */
    private static String resolveColumnName(Field field) {
        if (Objects.isNull(field)) {
            throw new RuntimeException("You can not have null field");
        }
        if (field.isAnnotationPresent(JoinColumn.class)) {
            return field.getAnnotation(JoinColumn.class).name();
        }

        return Optional.ofNullable(field.getAnnotation(Column.class))
                .map(Column::name)
                .orElse(field.getName());
    }

    /**
     * <p>This method returns a comma-separated list of the values of all the columns of the database table
     * corresponding to a given JPA entity that do not correspond to collection or primary key fields.</p>
     *
     * @param entity {@link Object} the JPA entity for which the list of column values should be returned.
     * @return {@link String} A comma-separated string containing the values of all the columns of the database table
     * corresponding to the given JPA entity that do not correspond to collection or primary key fields.
     */
    public static String getSqlFieldValuesWithoutId(Object entity) {
        var entityClass = entity.getClass();
        return Arrays.stream(entityClass.getDeclaredFields())
                .filter(field -> !isCollectionField(field) && !isIdField(field))
                .map(field -> getString(entity, field))
                .collect(Collectors.joining(","));
    }

    /**
     * <p>This method returns a comma-separated list of the values of all the columns of the database table
     * corresponding to a given JPA entity that do not correspond to collection fields.</p>
     *
     * @param entity {@link Object} the JPA entity for which the list of column values should be returned.
     * @return {@link String} A comma-separated string containing the values of all the columns of the database table
     * corresponding to the given JPA entity that do not correspond to collection or primary key fields.
     */
    public static String getSqlFieldValues(Object entity) {
        var entityClass = entity.getClass();
        return Arrays.stream(entityClass.getDeclaredFields())
                .filter(field -> !isCollectionField(field))
                .map(field -> getString(entity, field))
                .collect(Collectors.joining(","));
    }

    /**
     * <p>This method returns a string representation of the value of a given field of a JPA entity.
     * The representation is formatted as follows:</p>
     * <ol>
     *     <li>If the value is null, the string "null" is returned.</li>
     *     <li>If the value is a string, a LocalDate, or a LocalDateTime, the value is enclosed in single quotes (').</li>
     *     <li>If the value is a number, its string representation is returned.</li>
     *     <li>If the value is a JPA entity, the value of its primary key field is returned.</li>
     *     <li>Otherwise, the string representation of the value</li>
     * </ol>
     *
     * @param entity {@link Object} The entity object to get the field value from.
     * @param field  {@link Field} The field to get the value of.
     * @return A string representation of the value of the field.
     */

    public static String getString(Object entity, Field field) {
        var value = getFieldValue(entity, field);
        if (Objects.isNull(value)) {
            return "null";
        }
        if (value instanceof String || value instanceof LocalDate || value instanceof LocalDateTime) {
            return String.format("'%s'", value);
        }
        if (value instanceof Number) {
            return value.toString();
        }
        if (isParentEntityField(field) || isOneToOneEntityField(field)) {
            return getIdentifierValue(value).toString();
        }
        return value.toString();
    }

    /**
     * <p>Gets the value of a field from an object using reflection.</p>
     *
     * @param object  {@link Object}   the object to get the field value from
     * @param idField {@link Field}  the field to get the value of
     * @return the value {@link Object}  of the field in the object
     * @throws InternalException if the field value cannot be retrieved
     */
    public static Object getFieldValue(Object object, Field idField) {
        idField.setAccessible(true);
        try {
            return idField.get(object);
        } catch (IllegalAccessException e) {
            throw new InternalException("Can't get field %s".formatted(idField.getName()), "", e);
        }

    }

    /**
     * <p>Checks if a field is a parent entity field.</p>
     *
     * @param field {@link Field} the field to check
     * @return {@link Boolean#TRUE} if the field is a parent entity field; {@link Boolean#FALSE} otherwise
     */
    private static boolean isParentEntityField(Field field) {
        if (Objects.isNull(field)) {
            return false;
        }
        return field.isAnnotationPresent(ManyToOne.class);
    }

    private static boolean isOneToOneEntityField(Field field) {
        if (Objects.isNull(field)) {
            return false;
        }
        return field.isAnnotationPresent(OneToOne.class);
    }

    /**
     * <p>Gets the value of the identifier field of an entity object.</p>
     *
     * @param entity {@link Object} the entity object to get the identifier value from
     * @param <T>    the type of the entity object
     * @return the value of the identifier field in the entity object
     */
    public static <T> Object getIdentifierValue(T entity) {
        Field identifierField = getIdentifierField(entity.getClass());
        try {
            identifierField.setAccessible(true);
            return identifierField.get(entity);
        } catch (IllegalAccessException e) {
            var cause = "entity does not have id field value";
            var solution = "make sure that entity has id field value";
            throw new InternalException(cause, solution);
        }
    }

    /**
     * <p>Gets the identifier field of an entity class.</p>
     *
     * @param entityClass {@link Class} the entity class to get the identifier field from
     * @return the identifier field {@link Field} of the entity class
     * @throws InternalException if the entity class is not marked with @Id annotation
     */
    public static Field getIdentifierField(Class<?> entityClass) {
        var cause = "entity is not marked with Id annotation";
        var solution = "mark id column with Id annotation";
        return Arrays.stream(entityClass.getDeclaredFields())
                .filter(field -> field.isAnnotationPresent(Id.class))
                .findAny()
                .orElseThrow(() -> new InternalException(cause, solution));
    }

    /**
     * <p>returns a List of Field objects that have the {@link CascadeType#ALL} or {@link CascadeType#REMOVE} cascade type specified on their @OneToMany</p>
     *
     * @param entityClass - {@link Class}
     * @return {@link List} of {@link Field} list of field that have cascade described above
     */
    public static List<Field> getCascadeAllOrRemoveListFields(Class<?> entityClass) {
        var list = getCollectionFields(entityClass);
        return list.stream().filter(DaoUtils::isFieldAllOrRemoveCascade).toList();
    }

    /**
     * <p>Gets a list of collection fields from an entity class.</p>
     *
     * @param entityClass {@link Class} the entity class to get the collection fields from
     * @return a list of collection fields in the entity class
     */
    public static List<Field> getCollectionFields(Class<?> entityClass) {
        return Arrays.stream(entityClass.getDeclaredFields())
                .filter(field -> field.isAnnotationPresent(OneToMany.class))
                .toList();
    }

    public static CascadeType getCascadeType(Field field) {
        var cause = "OneToMany annotation does not have CascadeType";
        var solution = "annotate field with OneToMany annotation and put CascadeType on it";
        return Optional.ofNullable(field.getAnnotation(OneToMany.class))
                .orElseThrow(() -> new JdbcDaoException(cause, solution))
                .cascade();
    }

    public static boolean isFieldAllOrPersistCascade(Field field) {
        var cascadeType = getCascadeType(field);
        return cascadeType.equals(CascadeType.PERSIST) || cascadeType.equals(CascadeType.ALL);
    }

    static boolean isFieldAllOrRemoveCascade(Field field) {
        var cascadeType = getCascadeType(field);
        return cascadeType.equals(CascadeType.REMOVE) || cascadeType.equals(CascadeType.ALL);
    }

    public static boolean isFieldAllOrMergeCascade(Field field){
        var cascadeType = getCascadeType(field);
        return cascadeType.equals(CascadeType.MERGE) || cascadeType.equals(CascadeType.ALL);
    }

    /**
     * <p>Gets the name of the identifier field of an entity class.</p>
     *
     * @param entityClass {@link Class} the entity class to get the identifier field name from
     * @return the name {@link String} of the identifier field in the entity class
     */
    public static String getIdentifierFieldName(Class<?> entityClass) {
        Field field = getIdentifierField(entityClass);
        return field.getName();
    }

    /**
     * <p>Returns the name of the column in the database that corresponds to the given field.</p>
     *
     * @param field {@link Field} the field to get the column name for
     * @return the name {@link String} of the column in the database that corresponds to the given field
     */
    public static String getColumnName(Field field) {
        if (field.isAnnotationPresent(JoinColumn.class)) {
            return field.getAnnotation(JoinColumn.class).name();
        }
        return Optional.ofNullable(field.getAnnotation(Column.class)).map(Column::name).orElse(field.getName());
    }

    /**
     * <p>Returns the name of the table in the database that corresponds to the given entity class.</p>
     *
     * @param entityClass {@link Class} the entity class to get the table name for
     * @return the name {@link String} of the table in the database that corresponds to the given entity class
     */
    public static String getClassTableName(Class<?> entityClass) {
        return Optional.ofNullable(entityClass.getAnnotation(Table.class))
                .map(Table::name)
                .orElse(entityClass.getSimpleName());
    }

    /**
     * <p>Resolves the name of a field, taking into account any annotations that might override the default name.</p>
     *
     * @param field {@link Field} the field to resolve the name of
     * @return the resolved name {@link String} of the field
     */
    public static String resolveFieldName(Field field) {
        if (field.isAnnotationPresent(Column.class)) {
            String fieldName = field.getAnnotation(Column.class).name();
            if (!fieldName.isEmpty()) {
                return fieldName;
            }
        } else if (field.isAnnotationPresent(JoinColumn.class)) {
            String fieldName = field.getAnnotation(JoinColumn.class).name();
            if (!fieldName.isEmpty()) {
                return fieldName;
            }
        }
        return field.getName();
    }

    /**
     * <p>Determines whether the given field represents a regular (i.e., non-entity) field.</p>
     *
     * @param field - {@link Field} the field to check
     * @return {@link Boolean#TRUE} if the given field represents a regular field, {@link Boolean#FALSE} otherwise
     */
    public static boolean isRegularField(Field field) {
        return !isEntityField(field) && !isEntityCollectionField(field);
    }

    /**
     * <p>Determines whether the given field represents an entity field (i.e., a field that has the @ManyToOne or @OneToOne annotation).</p>
     *
     * @param field {@link Field} the field to check
     * @return {@link Boolean#TRUE} if the given field represents an entity field, {@link Boolean#FALSE} otherwise
     */
    public static boolean isEntityField(Field field) {
        return field.isAnnotationPresent(ManyToOne.class) || field.isAnnotationPresent(OneToOne.class);
    }

    /**
     * <p>Determines whether the given field represents an entity collection field (i.e., a field that has the @OneToMany annotation).</p>
     *
     * @param field {@link Field} the field to check
     * @return {@link Boolean#TRUE} if the given field represents an entity collection field, {@link Boolean#FALSE} otherwise
     */
    public static boolean isEntityCollectionField(Field field) {
        return field.isAnnotationPresent(OneToMany.class);
    }

    /**
     * <p>Determines whether the given field (annotated @OneToMany) defines `fetch` parameter as default lazy strategies for fetching data from BD.</p>
     *
     * @param field {@link Field} the field to check
     * @return {@link Boolean#TRUE} if the given field has {@link FetchType#LAZY} fetch strategy, {@link Boolean#FALSE} otherwise
     */
    public static boolean isEntityCollectionFieldIsLazy(Field field) {
        return field.getAnnotation(OneToMany.class).fetch() == FetchType.LAZY;
    }

    /**
     * <p>Set the value of a field annotated with a given annotation in an entity.</p>
     *
     * @param entity          {@link Object} the entity object to set the field value for
     * @param value           {@link Object} the value to be set to the field
     * @param annotationClass {@link Class} the class of the annotation that is present on the field to set the value for
     * @throws InternalException if the field with the given annotation is not found, or if the field cannot be accessed or set
     */
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
     * @param entity {@link Object} the entity object to set the field value for
     * @param value {@link Object} the value to be set to the field
     * @param field entity field
     * @throws InternalException if the field cannot be accessed or set
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

    /**
     * <p>Resolve the name of the table that an entity is mapped to.</p>
     *
     * @param entity {@link Object} the entity object to resolve the table name for
     * @return the name {@link String} of the table that the entity is mapped to
     * @throws JdbcDaoException if the entity class is not annotated with {@link Table}
     */
    public static String resolveTableName(Object entity) {
        var entityClass = entity.getClass();
        if (!entityClass.isAnnotationPresent(Table.class)) {
            throw new JdbcDaoException("entity is not marked with @Table annotation", "mark entity with table annotation");
        }
        return Optional.ofNullable(entityClass.getAnnotation(Table.class))
                .map(Table::name)
                .orElse(entityClass.getSimpleName());
    }

    /**
     * <p>This method returns the strategy used for generating values of the primary key for a given JPA entity.
     * It looks for fields in the entity class that are annotated with @GeneratedValue and returns
     * the strategy specified by that annotation. If no such field is found, it returns Strategy.AUTO</p>
     *
     * @param entity {@link Object} the JPA entity for which the strategy should be returned.
     * @return {@link Strategy} used for generating values of the primary key for the given JPA entity.
     */
    public static Strategy getStrategy(Object entity) {
        var entityClass = entity.getClass();
        return Arrays.stream(entityClass.getDeclaredFields())
                .filter(field -> field.isAnnotationPresent(GeneratedValue.class))
                .findAny()
                .map(field -> field.getAnnotation(GeneratedValue.class))
                .map(GeneratedValue::strategy).orElse(Strategy.AUTO);
    }

    /**
     * <p>Method gets the collection field of a related entity class.</p>
     *
     * @param fromEntity   {@link Class} the entity class to get the related entity field.
     * @param toEntityType {@link Class} the entity class to get the related entity collection field from.
     * @param <T>          related Entity Type
     * @return the related entity collection field {@link Field}
     */
    public static <T> Field getRelatedEntityField(Class<T> fromEntity, Class<?> toEntityType) {
        return Arrays.stream(toEntityType.getDeclaredFields())
                .filter(f -> f.getType().equals(fromEntity))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(String.format("Cannon find related field between in '%s' fro '%s'",
                        toEntityType.getSimpleName(), fromEntity.getSimpleName())));
    }

    /**
     * <p>Method gets class of the collection field of a related entity class.</p>
     *
     * @param field {@link Field} the field to get a related entity collection field type
     * @return {@link Class} the class of related entity collection field
     */
    public static Class<?> getEntityCollectionElementType(Field field) {
        var parameterizedType = (ParameterizedType) field.getGenericType();
        var actualTypeArgument = parameterizedType.getActualTypeArguments()[0];
        return (Class<?>) actualTypeArgument;
    }

    public static boolean isLoadedLazyList(Collection<?> collection) {
        if (collection instanceof LazyList<?> lazyList){
            return lazyList.isLoaded();
        }
        return true;
    }

    public static <T> T createEntityInstance(Class<T> entityClass) {
        try {
            return entityClass.getDeclaredConstructor().newInstance();
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            throw new InternalException(String.format("Can't create entity instance of type '%s'", entityClass),
                    "Entity class should have a default constructor");
        }
    }
}
