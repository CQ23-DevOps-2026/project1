package com.yas.rating.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.AuditorAware;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

class DatabaseAutoConfigTest {

    private DatabaseAutoConfig databaseAutoConfig;

    @BeforeEach
    void setUp() {
        databaseAutoConfig = new DatabaseAutoConfig();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void auditorAware_whenAuthenticationIsNull_shouldReturnEmptyString() {
        SecurityContextHolder.clearContext();
        
        AuditorAware<String> auditorAware = databaseAutoConfig.auditorAware();
        Optional<String> result = auditorAware.getCurrentAuditor();
        
        assertThat(result).isPresent().contains("");
    }

    @Test
    void auditorAware_whenAuthenticationIsPresent_shouldReturnName() {
        Authentication auth = mock(Authentication.class);
        when(auth.getName()).thenReturn("test-user");
        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(auth);
        SecurityContextHolder.setContext(securityContext);
        
        AuditorAware<String> auditorAware = databaseAutoConfig.auditorAware();
        Optional<String> result = auditorAware.getCurrentAuditor();
        
        assertThat(result).isPresent().contains("test-user");
    }
}
