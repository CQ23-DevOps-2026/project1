package com.yas.rating.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;

class SecurityConfigTest {

    @Test
    void filterChain_shouldReturnSecurityFilterChain() throws Exception {
        SecurityConfig securityConfig = new SecurityConfig();
        HttpSecurity httpSecurity = mock(HttpSecurity.class);
        SecurityFilterChain securityFilterChain = mock(SecurityFilterChain.class);

        when(httpSecurity.authorizeHttpRequests(org.mockito.ArgumentMatchers.any())).thenReturn(httpSecurity);
        when(httpSecurity.oauth2ResourceServer(org.mockito.ArgumentMatchers.any())).thenReturn(httpSecurity);
        when(httpSecurity.build()).thenReturn(securityFilterChain);

        SecurityFilterChain result = securityConfig.filterChain(httpSecurity);

        assertThat(result).isEqualTo(securityFilterChain);
    }

    @Test
    void jwtAuthenticationConverterForKeycloak_shouldReturnConverterWithMappedAuthorities() {
        SecurityConfig securityConfig = new SecurityConfig();
        JwtAuthenticationConverter converter = securityConfig.jwtAuthenticationConverterForKeycloak();

        Jwt jwt = mock(Jwt.class);
        Map<String, Object> realmAccess = Map.of("roles", List.of("ADMIN", "USER"));
        when(jwt.getClaim("realm_access")).thenReturn(realmAccess);

        // We can't easily test the internal converter directly without reflection or using the converter
        // But we can check if it works by using reflection to get the internal converter or just rely on standard behavior if possible.
        // Actually, JwtAuthenticationConverter has a method to convert.
        
        var authentication = converter.convert(jwt);
        Collection<GrantedAuthority> authorities = authentication.getAuthorities();

        assertThat(authorities).containsExactlyInAnyOrder(
                new SimpleGrantedAuthority("ROLE_ADMIN"),
                new SimpleGrantedAuthority("ROLE_USER")
        );
    }
}
