package com.breskul.bibernate.persistence.util;

import com.breskul.bibernate.annotation.GeneratedValue;
import com.breskul.bibernate.annotation.Id;
import com.breskul.bibernate.annotation.Strategy;
import com.breskul.bibernate.collection.LazyList;
import com.breskul.bibernate.exception.DaoUtilsException;
import com.breskul.bibernate.exception.InternalException;
import com.breskul.bibernate.persistence.test_model.Person;
import com.breskul.bibernate.persistence.util.test_model.*;
import jakarta.persistence.Column;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class DaoUtilsTest {

    @Test
    @DisplayName("Test getIdentifierField method")
    void testGetIdentifierField() {
        assertThrows(DaoUtilsException.class, () -> DaoUtils.getIdentifierField(Note.class));
    }

    @Test
    @DisplayName("Test getIdentifierFieldName method")
    void testGetIdentifierFieldName() {
        assertThrows(DaoUtilsException.class, () -> DaoUtils.getIdentifierFieldName(Note.class));
    }

    @Test
    @DisplayName("Test getIdentifierValue method")
    void testGetIdentifierValue() {
        Note note = new Note();
        note.setId(1L);
        note.setBody("my note");
        assertThrows(DaoUtilsException.class, () -> DaoUtils.getIdentifierValue(note));
    }

    @Test
    @DisplayName("Test setValueToField with Id annotation")
    void testSetValueToFieldWithIdAnnotation() {
        SetValueTest setValueTest = new SetValueTest();
        Long id = 1L;
        assertNull(setValueTest.getId());

        DaoUtils.setValueToField(setValueTest, id, Id.class);

        assertEquals(id, setValueTest.getId());
    }

    @Test
    @DisplayName("Test setValueToField with not exist annotation")
    void testSetValueToFieldWithNotExistAnnotation() {
        SetValueTest setValueTest = new SetValueTest();
        Long id = 1L;
        assertThrows(InternalException.class, () -> DaoUtils.setValueToField(setValueTest, id, Column.class));
    }

    @Test
    @DisplayName("Test setValueToField with passed Field to set")
    void testSetValueToFieldWithFieldPasses() throws NoSuchFieldException {
        SetValueTest setValueTest = new SetValueTest();
        Long id = 1L;
        assertNull(setValueTest.getId());

        Field field = setValueTest.getClass().getDeclaredField("id");
        DaoUtils.setValueToField(setValueTest, id, field);

        assertEquals(id, setValueTest.getId());
    }

    @Test
    @DisplayName("Test setValueToField with not correct value")
    void testSetValueToFieldWithNotCorrectValue() {
        SetValueTest setValueTest = new SetValueTest();
        String id = "test";
        assertThrows(IllegalArgumentException.class, () -> DaoUtils.setValueToField(setValueTest, id, Id.class));
    }
    @Test
    @DisplayName("Test strategy AUTO is detected")
    void testGetStrategyAuto() {
        class Entity {
            private Long id;
            private String name;
        }
        var entity = new Entity();
        var strategy = DaoUtils.getStrategy(entity);
        assertEquals(Strategy.AUTO, strategy);
    }

    @Test
    @DisplayName("Test strategy IDENTITY is detected")
    void testGetStrategyIdentity() {
        class Entity {
            @GeneratedValue(strategy = Strategy.IDENTITY)
            private Long id;
            private String name;
        }
        var entity = new Entity();
        var strategy = DaoUtils.getStrategy(entity);
        assertEquals(Strategy.IDENTITY, strategy);
    }

    @Test
    @DisplayName("Test strategy SEQUENCE is detected")
    void testGetStrategySequence() {
        class Entity {
            @GeneratedValue(strategy = Strategy.SEQUENCE)
            private Long id;
            private String name;
        }
        var entity = new Entity();
        var strategy = DaoUtils.getStrategy(entity);
        assertEquals(Strategy.SEQUENCE, strategy);
    }

    @Test
    @DisplayName("Test GetSqlFieldNames with SEQUENCE")
    void testGetSqlFieldNamesSequence() {
        var entity = new EntitySequence();
        var sqlFieldNames = DaoUtils.getSqlFieldNames(entity);
        assertEquals("name,age", sqlFieldNames);
    }

    @Test
    @DisplayName("Test GetSqlFieldNames with IDENTITY")
    void testGetSqlFieldNamesIdentity() {
        var entity = new EntityIdentity();
        var sqlFieldNames = DaoUtils.getSqlFieldNames(entity);
        assertEquals("name,age", sqlFieldNames);
    }


    @Test
    @DisplayName("Test get sql field names with AUTO")
    void testGetSqlFieldNamesAuto() {
        var entity = new EntityAuto();
        var sqlFieldNames = DaoUtils.getSqlFieldNames(entity);
        assertEquals("name,age", sqlFieldNames);
    }

    @Test
    @DisplayName("Test get string with null value")
    void testGetStringWithNullValue() throws Exception {
        class Entity {
            private String name;
        }
        var entity = new Entity();
        var field = entity.getClass().getDeclaredField("name");
        var result = DaoUtils.getString(entity, field);
        assertEquals("null", result);
    }

    @Test
    @DisplayName("Test get string with string value")
    void testGetStringWithStringValue() throws Exception {
        class Entity {
            private String name = "John";
        }
        var entity = new Entity();
        var field = entity.getClass().getDeclaredField("name");
        var result = DaoUtils.getString(entity, field);
        assertEquals("'John'", result);
    }

    @Test
    @DisplayName("Test get string with string value")
    void testGetStringWithLocalDateValue() throws Exception {
        class Entity {
            private LocalDate birthDate = LocalDate.of(1990, 1, 1);
        }
        var entity = new Entity();
        var field = entity.getClass().getDeclaredField("birthDate");
        var result = DaoUtils.getString(entity, field);
        assertEquals("'1990-01-01'", result);
    }

    @Test
    @DisplayName("Test get string with localDateTime value")
    void testGetStringWithLocalDateTimeValue() throws Exception {
        class Entity {
            private LocalDateTime updatedAt = LocalDateTime.of(2022, 4, 2, 12, 0);
        }
        var entity = new Entity();
        var field = entity.getClass().getDeclaredField("updatedAt");
        var result = DaoUtils.getString(entity, field);
        assertEquals("'2022-04-02T12:00'", result);
    }

    @Test
    @DisplayName("Test get string with number value")
    void testGetStringWithNumberValue() throws Exception {
        class Entity {
            private Integer age = 30;
        }
        var entity = new Entity();
        var field = entity.getClass().getDeclaredField("age");
        var result = DaoUtils.getString(entity, field);
        assertEquals("30", result);
    }

    @Test
    @DisplayName("Test get field value")
    void testGetFieldValue() throws Exception {
        class Entity {
            private String name = "John";
        }
        var entity = new Entity();
        var field = entity.getClass().getDeclaredField("name");
        var result = DaoUtils.getFieldValue(entity, field);
        assertEquals("John", result);
    }

    @Test
    @DisplayName("isLoadedList returns ture")
    void testIsLoadedListTrue() throws Exception {
        LazyList<String> lazyList = new LazyList<>(ArrayList::new);
        lazyList.add("foo");
        lazyList.add("bar");

        boolean result = DaoUtils.isLoadedLazyList(lazyList);

        Assertions.assertTrue(result);
    }

    @Test
    @DisplayName("isLoadedList returns false")
    void testIsLoadedListFalse() throws Exception {
        LazyList<String> lazyList = new LazyList<>(ArrayList::new);

        boolean result = DaoUtils.isLoadedLazyList(lazyList);

        Assertions.assertFalse(result);
    }

    @Test
    @DisplayName("Create entity instance")
    void testCreateEntityInstance() {
        Class<Person> entityClass = Person.class;

        Person person = DaoUtils.createEntityInstance(entityClass);

        Assertions.assertNotNull(person);
    }
}
