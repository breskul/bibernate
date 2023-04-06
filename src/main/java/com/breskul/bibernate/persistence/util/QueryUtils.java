package com.breskul.bibernate.persistence.util;

import java.util.ArrayList;
import java.util.List;

/**
 * Utils query class provide methods for generation sql queries
 */
public class QueryUtils {
    private QueryUtils() {}

    private static final String SELECT_FROM_TABLE_BY_COLUMN_STATEMENT = "SELECT %s.* FROM %s %s WHERE %s.%s = ?";
    private static final String DELETE_STATEMENT = "DELETE FROM %s WHERE %s = ?";
    private static final String INSERT_QUERY = "INSERT INTO %s (%s) VALUES (%s)";
    private static final String SELECT_SEQ_QUERY = "SELECT nextval('%s_seq')";
    private static final String UPDATE_QUERY = "UPDATE %s SET %s WHERE %s";

    /**
     * Generate update query for entity
     * @param entity updated entity
     * @return generated update query
     */
    public static String buildUpdateQuery(Object entity) {
        var tableName = DaoUtils.resolveTableName(entity);
        var identifierColumn = DaoUtils.getIdentifierFieldName(entity.getClass());
        var identifierValue = DaoUtils.getIdentifierValue(entity);
        String[] columns = DaoUtils.getSqlFieldNamesWithoutId(entity).split(",");
        String[] values = DaoUtils.getSqlFieldValuesWithoutId(entity).split(",");
        List<String> mapColumnsToValues = new ArrayList<>();
        for (int i = 0; i < columns.length; i++) {
            mapColumnsToValues.add(columns[i] + " = " + values[i]);
        }
        String condition = identifierColumn + " = " + identifierValue;
        return UPDATE_QUERY.formatted(tableName, String.join(", ", mapColumnsToValues), condition);
    }

    /**
     * Generate select query
     * @param tableName database table name
     * @param columnName search condition column
     * @return generated select query
     */
    public static String buildSelectQuery(String tableName, String columnName) {
        final var alias = tableName.substring(0, 1).toLowerCase();
        return String.format(SELECT_FROM_TABLE_BY_COLUMN_STATEMENT, alias, tableName, alias, alias, columnName);
    }

    /**
     * Generate delete query
     * @param tableName database table name
     * @param identifierName primary key
     * @return generated delete query
     */
    public static String buildDeleteQuery(String tableName, String identifierName) {
        return String.format(DELETE_STATEMENT, tableName, identifierName);
    }

    /**
     * Generate insert query
     * @param tableName database table name
     * @param sqlFieldNames columns for insert
     * @param sqlFieldValues values for insert
     * @return generated insert query
     */
    public static String buildInsertQuery(String tableName, String sqlFieldNames, String sqlFieldValues) {
        return String.format(INSERT_QUERY, tableName, sqlFieldNames, sqlFieldValues);
    }

    /**
     * Generate select sequence query
     * @param tableName database table name
     * @return generated select sequence query
     */
    public static String buildSequenceQuery(String tableName) {
        return String.format(SELECT_SEQ_QUERY, tableName);
    }
}
