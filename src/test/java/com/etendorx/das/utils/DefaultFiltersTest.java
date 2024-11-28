package com.etendorx.das.utils;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DefaultFiltersTest {

  @Test
  void testAddFilters_SuperUserBypass() {
    String sql = "select * from pet_owner o1_0";
    String result = DefaultFilters.addFilters(sql, "100", "0", "admin", true, DefaultFilters.GET_METHOD);

    assertEquals(sql, result, "SuperUser should bypass filters");
  }

  @Test
  void testAddFilters_GetMethodWithActiveFilter() {
    String sql = "select * from pet_owner o1_0";
    String expected = "select * from pet_owner o1_0 where o1_0.ad_client_id in ('0', '1') and etrx_role_organizations('1', 'admin', 'r') like concat('%|', o1_0.ad_org_id, '|%') and o1_0.isactive = 'Y'";

    String result = DefaultFilters.addFilters(sql, "user", "1", "admin", true, DefaultFilters.GET_METHOD);

    assertEquals(expected, result, "Filters should be correctly added for GET method");
  }

  @Test
  void testGetQueryInfo_SelectQuery() {
    String sql = "select * from pet_owner o1_0 where o1_0.city = 'Springfield'";

    DefaultFilters.QueryInfo queryInfo = DefaultFilters.getQueryInfo(sql);

    assertEquals("select", queryInfo.getSqlAction());
    assertEquals("pet_owner", queryInfo.getTableName());
    assertEquals("o1_0", queryInfo.getTableAlias());
    assertTrue(queryInfo.isContainsWhere(), "Query should contain a WHERE clause");
  }

  @Test
  void testGetQueryInfo_InsertQuery() {
    String sql = "insert into pet_owner (id, name) values (1, 'John')";

    DefaultFilters.QueryInfo queryInfo = DefaultFilters.getQueryInfo(sql);

    assertEquals("insert into", queryInfo.getSqlAction());
    assertEquals("pet_owner", queryInfo.getTableName());
    assertEquals("pet_owner", queryInfo.getTableAlias());
    assertFalse(queryInfo.isContainsWhere(), "Insert query should not contain a WHERE clause");
  }

  @Test
  void testReplaceInQuery_ValidQuery() {
    String sql = "select * from pet_owner o1_0";
    String result = DefaultFilters.addFilters(sql, "user", "1", "role", true, DefaultFilters.GET_METHOD);

    assertTrue(result.contains("o1_0.isactive = 'Y'"), "Filter for active records should be added");
  }

  @Test
  void testAddFilters_InvalidHttpMethod() {
    String sql = "select * from pet_owner o1_0";

    IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
        DefaultFilters.addFilters(sql, "user", "1", "role", true, "INVALID_METHOD")
    );

    assertEquals("Unknown HTTP method: INVALID_METHOD", exception.getMessage());
  }

}
