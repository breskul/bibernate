package com.breskul.bibernate.persistence.util;

import com.breskul.bibernate.annotation.Id;
import com.breskul.bibernate.exception.DaoUtilsException;
import com.breskul.bibernate.exception.InternalException;
import com.breskul.bibernate.persistence.util.testModel.Note;
import com.breskul.bibernate.persistence.util.testModel.SetValueTest;
import jakarta.persistence.Column;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

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
}
