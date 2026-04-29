package com.yas.cart.config;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;

class SecurityConfigTest {

    @Test
    void securityConfig_canBeInstantiated() {
        SecurityConfig config = new SecurityConfig();
        assertThat(config).isNotNull();
    }

    @Test
    void jwtAuthenticationConverterForKeycloak_shouldMapRoles() {
        SecurityConfig config = new SecurityConfig();

        Jwt jwt = Jwt.withTokenValue("token")
            .header("alg", "none")
            .claim("realm_access", Map.of("roles", List.of("ADMIN", "CUSTOMER")))
            .build();

        var authentication = config.jwtAuthenticationConverterForKeycloak().convert(jwt);

        assertThat(authentication.getAuthorities())
            .extracting(GrantedAuthority::getAuthority)
            .contains("ROLE_ADMIN", "ROLE_CUSTOMER");
    }

    @Test
    void jwtAuthenticationConverterForKeycloak_shouldReturnConverter() {
        SecurityConfig config = new SecurityConfig();
        JwtAuthenticationConverter converter = config.jwtAuthenticationConverterForKeycloak();
        assertThat(converter).isNotNull();
    }

    @Test
    void jwtAuthenticationConverterForKeycloak_withSingleRole_shouldMapCorrectly() {
        SecurityConfig config = new SecurityConfig();

        Jwt jwt = Jwt.withTokenValue("token")
            .header("alg", "none")
            .claim("realm_access", Map.of("roles", List.of("USER")))
            .build();

        var authentication = config.jwtAuthenticationConverterForKeycloak().convert(jwt);

        assertThat(authentication.getAuthorities())
            .extracting(GrantedAuthority::getAuthority)
            .containsExactly("ROLE_USER");
    }
}
