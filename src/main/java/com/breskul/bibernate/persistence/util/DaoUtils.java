package com.breskul.bibernate.persistence.util;

import com.breskul.bibernate.annotations.Id;
import com.breskul.bibernate.annotations.Table;
import com.breskul.bibernate.exeptions.DaoUtilsException;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Optional;

public class DaoUtils {

    public static Field getIdentifierField(Class<?> entityClass){
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
}
