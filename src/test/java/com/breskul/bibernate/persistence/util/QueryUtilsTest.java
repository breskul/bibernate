package com.breskul.bibernate.persistence.util;

import com.breskul.bibernate.persistence.util.test_model.UpdateQueryTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class QueryUtilsTest {

    @Test
    @DisplayName("Get update query")
    public void getUpdateQuery() {
        String updateQuery = "UPDATE test SET first_name = 'firstName', last_name = 'lastName' WHERE id = 1";
        String result = QueryUtils.buildUpdateQuery(new UpdateQueryTest());
        assertEquals(updateQuery, result);
    }

    @Test
    @DisplayName("Get select query")
    public void getSelectQuery() {
        String selectQuery = "SELECT t.* FROM table t WHERE t.column = ?";;
        String result = QueryUtils.buildSelectQuery("table", "column");
        assertEquals(selectQuery, result);
    }

    @Test
    @DisplayName("Get delete query")
    public void getDeleteQuery() {
        String deleteQuery = "DELETE FROM table WHERE column = ?";
        String result = QueryUtils.buildDeleteQuery("table", "column");
        assertEquals(deleteQuery, result);
    }

    @Test
    @DisplayName("Get insert query")
    public void getInsertQuery() {
        String insertQuery = "INSERT INTO table (column1,column2,column3) VALUES (value1,value2,value3)";
        var tableName = "table";
        var columns = "column1,column2,column3";
        var values = "value1,value2,value3";
        String result = QueryUtils.buildInsertQuery(tableName, columns, values);
        assertEquals(insertQuery, result);
    }

    @Test
    @DisplayName("Get sequence query")
    public void getSequenceQuery() {
        String sequenceQuery = "SELECT nextval('table_seq')";
        String result = QueryUtils.buildSequenceQuery("table");
        assertEquals(sequenceQuery, result);
    }
}
