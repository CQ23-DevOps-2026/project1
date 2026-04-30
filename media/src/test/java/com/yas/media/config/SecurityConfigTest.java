package com.yas.media.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;

class SecurityConfigTest {

    private final SecurityConfig securityConfig = new SecurityConfig();

    @Test
    void testJwtAuthenticationConverterForKeycloak() {
        JwtAuthenticationConverter converter = securityConfig.jwtAuthenticationConverterForKeycloak();
        assertNotNull(converter);
    }

    @Test
    void testJwtGrantedAuthoritiesConverter() {
        // The converter is wrapped inside JwtAuthenticationConverter, but we can't easily extract it without reflection.
        // Instead, we will directly test the lambda behavior by recreating it here if necessary, or we can use reflection,
        // but wait, we can just execute a conversion by calling convert() on the JwtAuthenticationConverter!
        
        JwtAuthenticationConverter converter = securityConfig.jwtAuthenticationConverterForKeycloak();
        
        // Create a fake JWT
        Jwt jwt = Jwt.withTokenValue("fake-token")
                .header("alg", "none")
                .claim("realm_access", Map.of("roles", List.of("ADMIN", "USER")))
                .build();
                
        // Convert using the converter we just created
        // Wait, the JwtAuthenticationConverter calls the grantedAuthoritiesConverter internally, but it ALSO extracts standard scopes!
        // Actually, calling extractAuthorities is private/protected, but we can call convert() which returns an AbstractAuthenticationToken
        var authToken = converter.convert(jwt);
        
        assertNotNull(authToken);
        Collection<GrantedAuthority> authorities = authToken.getAuthorities();
        
        assertEquals(2, authorities.size());
        assertTrue(authorities.stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN")));
        assertTrue(authorities.stream().anyMatch(a -> a.getAuthority().equals("ROLE_USER")));
    }
}
