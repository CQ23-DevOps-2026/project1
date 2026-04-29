package com.yas.cart.config;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

class DatabaseAutoConfigTest {

    private final DatabaseAutoConfig config = new DatabaseAutoConfig();

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void auditorAware_whenNoAuthentication_returnsEmptyString() {
        SecurityContextHolder.clearContext();

        Optional<String> auditor = config.auditorAware().getCurrentAuditor();

        assertThat(auditor).contains("");
    }

    @Test
    void auditorAware_whenAuthenticationPresent_returnsName() {
        var auth = new UsernamePasswordAuthenticationToken("user-1", "n/a");
        SecurityContextHolder.getContext().setAuthentication(auth);

        Optional<String> auditor = config.auditorAware().getCurrentAuditor();

        assertThat(auditor).contains("user-1");
    }
}
