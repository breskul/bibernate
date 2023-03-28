package com.breskul.bibernate.persistence.util;

import com.breskul.bibernate.exception.DaoUtilsException;
import com.breskul.bibernate.persistence.util.testModel.Note;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.ClassOrderer;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestClassOrder;


@TestClassOrder(ClassOrderer.OrderAnnotation.class)
class DaoUtilsTest {

    @Test
    @Order(1)
    @DisplayName("1. Test getIdentifierField method")
    void testGetIdentifierField() {
        Assertions.assertThrows(DaoUtilsException.class, () -> DaoUtils.getIdentifierField(Note.class));
    }

    @Test
    @Order(2)
    @DisplayName("2. Test getIdentifierFieldName method")
    void testGetIdentifierFieldName() {
        Assertions.assertThrows(DaoUtilsException.class, () -> DaoUtils.getIdentifierFieldName(Note.class));
    }

    @Test
    @Order(3)
    @DisplayName("3. Test getIdentifierValue method")
    void testGetIdentifierValue() {
        Note note = new Note();
        note.setId(1L);
        note.setBody("my note");
        Assertions.assertThrows(DaoUtilsException.class, () -> DaoUtils.getIdentifierValue(note));
    }
}
