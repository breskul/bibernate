package com.breskul.bibernate.persistence.util;

import com.breskul.bibernate.exeptions.DaoUtilsException;
import com.breskul.bibernate.persistence.util.testModel.Note;
import org.junit.jupiter.api.*;


@TestClassOrder(ClassOrderer.OrderAnnotation.class)
public class DaoUtilsTest {

    @Test
    @Order(1)
    @DisplayName("1. Test getIdentifierField method")
    public void testGetIdentifierField() {
        Assertions.assertThrows(DaoUtilsException.class, () -> DaoUtils.getIdentifierField(Note.class));
    }

    @Test
    @Order(2)
    @DisplayName("2. Test getIdentifierFieldName method")
    public void testGetIdentifierFieldName() {
        Assertions.assertThrows(DaoUtilsException.class, () -> DaoUtils.getIdentifierFieldName(Note.class));
    }

    @Test
    @Order(3)
    @DisplayName("2. Test getIdentifierValue method")
    public void testGetIdentifierValue() {
        Note note = new Note();
        note.setId(1L);
        note.setBody("my note");
        Assertions.assertThrows(DaoUtilsException.class, () -> DaoUtils.getIdentifierValue(note));
    }
}
