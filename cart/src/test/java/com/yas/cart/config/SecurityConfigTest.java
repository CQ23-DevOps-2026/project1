package com.yas.cart.config;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;

class SecurityConfigTest {

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
            .containsExactlyInAnyOrder("ROLE_ADMIN", "ROLE_CUSTOMER");
    }
}
