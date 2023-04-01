package com.breskul.bibernate.persistence.util;

import com.breskul.bibernate.annotation.*;
import com.breskul.bibernate.exception.DaoUtilsException;
import com.breskul.bibernate.exception.InternalException;
import com.breskul.bibernate.exception.JdbcDaoException;

import java.lang.reflect.Field;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

public class DaoUtils {
    private DaoUtils() {
    }

    public static boolean isSequenceStrategy(Object entity) {
        var entityClass = entity.getClass();
        var strategyOptional = Arrays.stream(entityClass.getDeclaredFields())
                .filter(field -> field.isAnnotationPresent(GeneratedValue.class))
                .findAny()
                .map(field -> field.getAnnotation(GeneratedValue.class))
                .map(GeneratedValue::strategy);
        return strategyOptional
                .orElseThrow(() -> new RuntimeException("Every entity should annotated with @GeneratedValue annotation"))
                .equals(Strategy.SEQUENCE);
    }

    public static String getSqlFieldNames(Object entity) {
        var entityClass = entity.getClass();
        return Arrays.stream(entityClass.getDeclaredFields())
                .filter(field -> !isCollectionField(field) && !isIdField(field))
                .map(DaoUtils::resolveColumnName)
                .collect(Collectors.joining(","));

    }

    public static boolean isCollectionField(Field field) {
        return field.isAnnotationPresent(OneToMany.class);
    }

    public static boolean isIdField(Field field) {
        return field.isAnnotationPresent(Id.class);
    }

    public static String resolveColumnName(Field field) {
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

    public static String getSqlFieldValues(Object entity) {
        var entityClass = entity.getClass();
        return Arrays.stream(entityClass.getDeclaredFields())
                .filter(field -> !isCollectionField(field) && !isIdField(field))
                .map(field -> getString(entity, field))
                .collect(Collectors.joining(","));

    }

    private static String getString(Object entity, Field field) {
        var value = getFieldValue(entity, field);
        if (value instanceof String || value instanceof LocalDate || value instanceof LocalDateTime) {
            return String.format("'%s'", value);
        }
        if (value instanceof Number){
            return value.toString();
        }
        if (isParentEntityField(field)){
            return getIdentifierValue(value).toString();
        }
        return value.toString();
    }

    public static Object getFieldValue(Object object, Field idField) {
        idField.setAccessible(true);
        try {
            return idField.get(object);
        } catch (IllegalAccessException e) {
            throw new InternalException("Can't get field %s".formatted(idField.getName()), "", e);
        }

    }

    public static List<Field> getCollectionFields(Class<?> entityClass) {
        return Arrays.stream(entityClass.getDeclaredFields())
                .filter(field -> field.isAnnotationPresent(OneToMany.class))
                .toList();
    }

    public static String getIdentifierFieldName(Class<?> entityClass) {
        Field field = getIdentifierField(entityClass);
        return field.getName();
    }

    public static Field getIdentifierField(Class<?> entityClass) {
        var cause = "entity is not marked with Id annotation";
        var solution = "mark id column with Id annotation";
        return Arrays.stream(entityClass.getDeclaredFields())
                .filter(field -> field.isAnnotationPresent(Id.class))
                .findAny()
                .orElseThrow(() -> new DaoUtilsException(cause, solution));
    }

    public static String resolveTableName(Object entity) {
        var entityClass = entity.getClass();
        if (!entityClass.isAnnotationPresent(Table.class)) {
            throw new JdbcDaoException("entity is not marked with @Table annotation", "mark entity with table annotation");
        }
        return Optional.ofNullable(entityClass.getAnnotation(Table.class))
                .map(Table::name)
                .orElse(entityClass.getSimpleName());

    }

    public static <T> void isValidEntity(T entity) {
        var type = entity.getClass();
        if (!type.isAnnotationPresent(Entity.class)) {
            throw new JdbcDaoException("%s is not a valid entity class".formatted(type.getName()), "@Entity annotation should be present");
        }
        long idAnnotationCount = Arrays.stream(type.getDeclaredFields()).filter(field -> field.isAnnotationPresent(Id.class)).count();
        if (idAnnotationCount > 1) {
            throw new JdbcDaoException("There are more than one @Id annotation for %s".formatted(type.getName()), "Make sure that only one @Id annotation present");
        } else if (idAnnotationCount == 0) {
            throw new JdbcDaoException("There is no @Id annotation for %s".formatted(type.getName()), "Make sure that only one @Id annotation present");
        }

        Object id = getIdentifierValue(entity);
        if (!Objects.isNull(id)) {
            throw new JdbcDaoException("detached entity is passed to persist",  "Make sure that you don't set id manually when using @GeneratedValue");
        }

        Field identifierField = getIdentifierField(entity.getClass());
        if (!identifierField.isAnnotationPresent(GeneratedValue.class)){
            throw new JdbcDaoException("annotation GeneratedValue is not found" , "mark class with the GeneratedValue annotation");
        }

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

    public static boolean isParentEntityField(Field field) {
        return field.isAnnotationPresent(ManyToOne.class);
    }

}
