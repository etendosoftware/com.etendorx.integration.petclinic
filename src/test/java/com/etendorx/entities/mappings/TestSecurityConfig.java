package com.etendorx.entities.mappings;

import com.etendorx.utils.auth.key.context.AppContext;
import com.etendorx.utils.auth.key.context.FilterContext;
import com.etendorx.utils.auth.key.context.UserContext;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

import java.io.IOException;

@TestConfiguration
public class TestSecurityConfig {

  public static final String AD_USER_ID = "A530AAE22C864702B7E1C22D58E7B17B";
  public static final String AD_CLIENT_ID = "23C59575B9CF467C9620760EB255B389";
  public static final String AD_ORG_ID = "19404EAD144C49A0AF37D54377CF452D";

  @Bean
  public FilterContext filterContext() {
    return new FilterContext() {
      @Override
      protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
          FilterChain filterChain)
          throws ServletException, IOException {
        filterChain.doFilter(request, response);
      }
    };
  }

  @Bean
  @Primary
  public UserContext setupUserContext() {
    UserContext mockUserContext = new UserContext();
    mockUserContext.setUserId(AD_USER_ID);
    mockUserContext.setUserName("F&BAdmin");
    mockUserContext.setAuthToken("mock-token");
    mockUserContext.setRestMethod("POST");
    mockUserContext.setRestUri("/mock/api");
    mockUserContext.setClientId(AD_CLIENT_ID);
    mockUserContext.setOrganizationId(AD_ORG_ID);
    mockUserContext.setRoleId("42D0EEB1C66F497A90DD526DC597E6F0");
    mockUserContext.setExternalSystemId("123");

    AppContext.setCurrentUser(mockUserContext);
    return mockUserContext;
  }
}
