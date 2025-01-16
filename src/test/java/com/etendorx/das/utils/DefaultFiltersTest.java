package com.etendorx.das.utils;

import org.hibernate.QueryException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.Statement;

class DefaultFiltersTest {

  public static final String SELECT_PETOWNER = "select * from pet_owner o1_0";
  public static final String ADMIN = "admin";
  public static final String USER_100 = "100";
  public static final String CLIENT_0 = "0";
  public static final String PET_OWNER = "pet_owner";

  @Test
  void testAddFiltersSuperUserBypass() {
    String sql = SELECT_PETOWNER;
    String result = DefaultFilters.addFilters(sql, USER_100, CLIENT_0, ADMIN, true, DefaultFilters.GET_METHOD);

    assertEquals(sql, result, "SuperUser should bypass filters");

    // Additional edge case tests
    String emptySql = "";
    result = DefaultFilters.addFilters(emptySql, USER_100, CLIENT_0, ADMIN, true, DefaultFilters.GET_METHOD);
    assertEquals(emptySql, result, "SuperUser should bypass filters even with empty SQL");

    String nullSql = null;
    result = DefaultFilters.addFilters(nullSql, USER_100, CLIENT_0, ADMIN, true, DefaultFilters.GET_METHOD);
    assertNull(result, "SuperUser should bypass filters even with null SQL");
  }

  @Test
  void testGetQueryInfoSelectQuery() {
    String sql = SELECT_PETOWNER + " where o1_0.city = 'Springfield'";
    try {
      Statement statement = CCJSqlParserUtil.parse(sql);
      DefaultFilters.QueryInfo queryInfo = DefaultFilters.getQueryInfo(statement);

      assertEquals("select", queryInfo.getSqlAction());
      assertEquals(PET_OWNER, queryInfo.getTableName());
      assertEquals("o1_0", queryInfo.getTableAlias());
      assertTrue(queryInfo.isContainsWhere(), "Query should contain a WHERE clause");
    } catch (JSQLParserException e) {
      throw new QueryException("testGetQueryInfoSelectQuery ERROR");
    }
  }

  @Test
  void testGetQueryInfoInsertQuery() {
    String sql = "insert into pet_owner (id, name) values (1, 'John')";
    try {
      Statement statement = CCJSqlParserUtil.parse(sql);
      DefaultFilters.QueryInfo queryInfo = DefaultFilters.getQueryInfo(statement);

      assertEquals("insert into", queryInfo.getSqlAction());
      assertEquals(PET_OWNER, queryInfo.getTableName());
      assertEquals(PET_OWNER, queryInfo.getTableAlias());
      assertFalse(queryInfo.isContainsWhere(), "Insert query should not contain a WHERE clause");
    } catch (JSQLParserException e) {
      throw new QueryException("testGetQueryInfoSelectQuery ERROR");
    }
  }

  @Test
  void testAddFiltersInvalidHttpMethod() {
    String sql = SELECT_PETOWNER;

    IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
        DefaultFilters.addFilters(sql, "user", "1", "role", true, "INVALID_METHOD")
    );

    assertEquals("Unknown HTTP method: INVALID_METHOD", exception.getMessage());
  }

}
