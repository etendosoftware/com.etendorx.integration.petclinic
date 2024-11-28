package com.etendorx.das.utils;

import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
  void testAddFiltersGetMethodWithActiveFilter() {
    String sql = SELECT_PETOWNER;
    String expected = SELECT_PETOWNER + " where o1_0.ad_client_id in ('0', '1') and etrx_role_organizations('1', 'admin', 'r') like concat('%|', o1_0.ad_org_id, '|%') and o1_0.isactive = 'Y'";

    String result = DefaultFilters.addFilters(sql, "user", "1", ADMIN, true, DefaultFilters.GET_METHOD);

    assertEquals(expected, result, "Filters should be correctly added for GET method");
  }

  @Test
  void testGetQueryInfoSelectQuery() {
    String sql = SELECT_PETOWNER + " where o1_0.city = 'Springfield'";

    DefaultFilters.QueryInfo queryInfo = DefaultFilters.getQueryInfo(sql);

    assertEquals("select", queryInfo.getSqlAction());
    assertEquals(PET_OWNER, queryInfo.getTableName());
    assertEquals("o1_0", queryInfo.getTableAlias());
    assertTrue(queryInfo.isContainsWhere(), "Query should contain a WHERE clause");
  }

  @Test
  void testGetQueryInfoInsertQuery() {
    String sql = "insert into pet_owner (id, name) values (1, 'John')";

    DefaultFilters.QueryInfo queryInfo = DefaultFilters.getQueryInfo(sql);

    assertEquals("insert into", queryInfo.getSqlAction());
    assertEquals(PET_OWNER, queryInfo.getTableName());
    assertEquals(PET_OWNER, queryInfo.getTableAlias());
    assertFalse(queryInfo.isContainsWhere(), "Insert query should not contain a WHERE clause");
  }

  @Test
  void testReplaceInQueryValidQuery() {
    String sql = SELECT_PETOWNER;
    String result = DefaultFilters.addFilters(sql, "user", "1", "role", true, DefaultFilters.GET_METHOD);

    assertTrue(StringUtils.contains(result, "o1_0.isactive = 'Y'"), "Filter for active records should be added");
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
