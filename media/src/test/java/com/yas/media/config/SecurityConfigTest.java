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
import org.springframework.security.config.annotation.web.builders.HttpSecurity;

class SecurityConfigTest {

    private final SecurityConfig securityConfig = new SecurityConfig();


    @Test
    @SuppressWarnings("unchecked")
    void testFilterChain() throws Exception {
        HttpSecurity http = mock(HttpSecurity.class);
        
        // Mock the chaining
        org.mockito.Mockito.when(http.authorizeHttpRequests(org.mockito.ArgumentMatchers.any())).thenReturn(http);
        org.mockito.Mockito.when(http.oauth2ResourceServer(org.mockito.ArgumentMatchers.any())).thenReturn(http);

        // Capture the customize lambdas
        org.mockito.ArgumentCaptor<org.springframework.security.config.Customizer> authCaptor = 
            org.mockito.ArgumentCaptor.forClass(org.springframework.security.config.Customizer.class);
        org.mockito.ArgumentCaptor<org.springframework.security.config.Customizer> oauth2Captor = 
            org.mockito.ArgumentCaptor.forClass(org.springframework.security.config.Customizer.class);

        // Call the method
        securityConfig.filterChain(http);

        // Verify and capture
        org.mockito.Mockito.verify(http).authorizeHttpRequests(authCaptor.capture());
        org.mockito.Mockito.verify(http).oauth2ResourceServer(oauth2Captor.capture());

        // Execute authorizeHttpRequests lambda
        org.springframework.security.config.Customizer authCustomizer = authCaptor.getValue();
        Object authRegistryMock = mock(org.springframework.security.config.annotation.web.configurers.AuthorizeHttpRequestsConfigurer.AuthorizationManagerRequestMatcherRegistry.class, org.mockito.Mockito.RETURNS_DEEP_STUBS);
        authCustomizer.customize(authRegistryMock);

        // Execute oauth2ResourceServer lambda
        org.springframework.security.config.Customizer oauth2Customizer = oauth2Captor.getValue();
        Object oauth2ConfigurerMock = mock(org.springframework.security.config.annotation.web.configurers.oauth2.server.resource.OAuth2ResourceServerConfigurer.class, org.mockito.Mockito.RETURNS_DEEP_STUBS);
        oauth2Customizer.customize(oauth2ConfigurerMock);
    }

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
        
        assertTrue(authorities.size() >= 2);
        assertTrue(authorities.stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN")));
        assertTrue(authorities.stream().anyMatch(a -> a.getAuthority().equals("ROLE_USER")));
    }
}
