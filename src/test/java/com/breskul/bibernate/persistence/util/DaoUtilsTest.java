package com.breskul.bibernate.persistence.util;

import com.breskul.bibernate.annotation.*;
import com.breskul.bibernate.annotation.enums.CascadeType;
import com.breskul.bibernate.annotation.enums.Strategy;
import com.breskul.bibernate.exception.InternalException;
import com.breskul.bibernate.exception.JdbcDaoException;
import com.breskul.bibernate.persistence.util.test_model.*;
import jakarta.persistence.Column;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class DaoUtilsTest {

    @Test
    @DisplayName("Test getIdentifierField method")
    void testGetIdentifierField() {
        assertThrows(InternalException.class, () -> DaoUtils.getIdentifierField(Note.class));
    }

    @Test
    @DisplayName("Test getIdentifierFieldName method")
    void testGetIdentifierFieldName() {
        assertThrows(InternalException.class, () -> DaoUtils.getIdentifierFieldName(Note.class));
    }

    @Test
    @DisplayName("Test getIdentifierValue method")
    void testGetIdentifierValue() {
        Note note = new Note();
        note.setId(1L);
        note.setBody("my note");
        assertThrows(InternalException.class, () -> DaoUtils.getIdentifierValue(note));
    }

    @Test
    @DisplayName("Test setValueToField")
    void testSetValueToField() {
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
        var sqlFieldNames = DaoUtils.getSqlFieldNamesWithoutId(entity);
        assertEquals("name,age", sqlFieldNames);
    }

    @Test
    @DisplayName("Test GetSqlFieldNames with IDENTITY")
    void testGetSqlFieldNamesIdentity() {
        var entity = new EntityIdentity();
        var sqlFieldNames = DaoUtils.getSqlFieldNamesWithoutId(entity);
        assertEquals("name,age", sqlFieldNames);
    }


    @Test
    @DisplayName("Test get sql field names with AUTO")
    void testGetSqlFieldNamesAuto() {
        var entity = new EntityAuto();
        var sqlFieldNames = DaoUtils.getSqlFieldNamesWithoutId(entity);
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
    @DisplayName("Test get field value")
    public void testGetCollectionFields() {
        List<Field> fields = DaoUtils.getCollectionFields(Entity.class);
        assertEquals(2, fields.size());
        assertTrue(fields.stream().allMatch(field -> field.isAnnotationPresent(OneToMany.class)));
    }

    static class ParentEntity {
        @Id
        Long id;

        @OneToMany(cascade = CascadeType.ALL)
        List<ChildEntity> allChildren;

        @OneToMany(cascade = CascadeType.REMOVE)
        List<ChildEntity> removeChildren;

        @OneToMany(cascade = CascadeType.MERGE)
        List<ChildEntity> mergeChildren;

        @OneToMany
        List<ChildEntity> noCascadeChildren;
    }

    static class ChildEntity {
        @Id
        Long id;

        String name;
    }
    @Test
    @DisplayName("test getCascadeAllOrRemoveListFields method")
    void testGetCascadeAllOrRemoveListFields() {
        List<Field> fields = DaoUtils.getCascadeAllOrRemoveListFields(ParentEntity.class);
        assertEquals(3, fields.size());
        assertTrue(fields.stream().anyMatch(f -> f.getName().equals("allChildren")));
        assertTrue(fields.stream().anyMatch(f -> f.getName().equals("removeChildren")));
        assertFalse(fields.stream().anyMatch(f -> f.getName().equals("mergeChildren")));
        assertTrue(fields.stream().anyMatch(f -> f.getName().equals("noCascadeChildren")));
    }

    @Test
    @DisplayName("test getCascadeType method")
    public void testGetCascadeType() {
        Field field1 = getFieldFromEntity("relatedEntities");
        CascadeType cascadeType = DaoUtils.getCascadeType(field1);
        assertEquals(CascadeType.ALL, cascadeType);

        Field field2 = getFieldFromEntity("otherRelatedEntities");
        cascadeType = DaoUtils.getCascadeType(field2);
        assertEquals(CascadeType.REMOVE, cascadeType);

        // Test that exception is thrown if OneToMany annotation does not have CascadeType
        Field field3 = getFieldFromEntity("unrelatedEntities");
        assertThrows(JdbcDaoException.class, () -> DaoUtils.getCascadeType(field3));
    }

    @Test
    @DisplayName("test isFieldAllOrRemoveCascade method")
    public void testIsFieldAllOrRemoveCascade() {
        Field field1 = getFieldFromEntity("relatedEntities");
        boolean isAllOrRemoveCascade = DaoUtils.isFieldAllOrRemoveCascade(field1);
        assertTrue(isAllOrRemoveCascade);

        Field field2 = getFieldFromEntity("otherRelatedEntities");
        isAllOrRemoveCascade = DaoUtils.isFieldAllOrRemoveCascade(field2);
        assertTrue(isAllOrRemoveCascade);

        Field field3 = getFieldFromEntity("unrelatedEntities");
        assertThrows(JdbcDaoException.class, () -> DaoUtils.isFieldAllOrRemoveCascade(field3));
    }

    // Helper method to retrieve a field from the Entity class
    private Field getFieldFromEntity(String fieldName) {
        try {
            return Entity.class.getDeclaredField(fieldName);
        } catch (NoSuchFieldException e) {
            throw new RuntimeException("Field not found: " + fieldName);
        }
    }

    // Sample entity class for testing
    private static class Entity {
        @OneToMany(cascade = CascadeType.ALL)
        private List<RelatedEntity> relatedEntities;

        @OneToMany(cascade = CascadeType.REMOVE)
        private Set<OtherRelatedEntity> otherRelatedEntities;

        private String unrelatedEntities;
    }

    // Sample related entity classes for testing
    private static class RelatedEntity {}

    private static class OtherRelatedEntity {}



}
