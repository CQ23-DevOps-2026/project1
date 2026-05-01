package com.yas.rating.utils;

import com.yas.commonlibrary.exception.AccessDeniedException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import java.lang.reflect.Constructor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AuthenticationUtilsTest {

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void extractUserId_withJwtAuthenticationToken_shouldReturnSubject() {
        Jwt jwt = mock(Jwt.class);
        when(jwt.getSubject()).thenReturn("user-id");
        JwtAuthenticationToken auth = mock(JwtAuthenticationToken.class);
        when(auth.getToken()).thenReturn(jwt);

        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(auth);
        SecurityContextHolder.setContext(securityContext);

        String userId = AuthenticationUtils.extractUserId();
        assertThat(userId).isEqualTo("user-id");
    }

    @Test
    void extractUserId_withAnonymousAuthenticationToken_shouldThrowAccessDeniedException() {
        AnonymousAuthenticationToken auth = mock(AnonymousAuthenticationToken.class);
        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(auth);
        SecurityContextHolder.setContext(securityContext);

        AccessDeniedException exception = assertThrows(AccessDeniedException.class, AuthenticationUtils::extractUserId);
        assertThat(exception.getMessage()).isEqualTo("ACCESS_DENIED");
    }

    @Test
    void testPrivateConstructor() throws Exception {
        Constructor<AuthenticationUtils> constructor = AuthenticationUtils.class.getDeclaredConstructor();
        constructor.setAccessible(true);
        AuthenticationUtils instance = constructor.newInstance();
        assertThat(instance).isNotNull();
    }
}
