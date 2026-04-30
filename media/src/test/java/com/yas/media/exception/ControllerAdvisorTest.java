package com.yas.media.exception;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.yas.commonlibrary.exception.NotFoundException;
import com.yas.commonlibrary.exception.UnsupportedMediaTypeException;
import com.yas.media.viewmodel.ErrorVm;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import java.util.Set;
import org.hibernate.validator.internal.engine.path.PathImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;

class ControllerAdvisorTest {

    private ControllerAdvisor controllerAdvisor;

    @BeforeEach
    void setUp() {
        controllerAdvisor = new ControllerAdvisor();
    }

    private WebRequest mockWebRequest(String path) {
        ServletWebRequest webRequest = mock(ServletWebRequest.class);
        HttpServletRequest httpServletRequest = mock(HttpServletRequest.class);
        when(httpServletRequest.getServletPath()).thenReturn(path);
        when(webRequest.getRequest()).thenReturn(httpServletRequest);
        return webRequest;
    }

    @Test
    void handleUnsupportedMediaTypeException_thenReturn400() {
        UnsupportedMediaTypeException ex = new UnsupportedMediaTypeException("Unsupported type");
        WebRequest request = mockWebRequest("/medias");

        ResponseEntity<ErrorVm> response = controllerAdvisor.handleUnsupportedMediaTypeException(ex, request);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        // ErrorVm uses 'detail' field
        assertEquals("File uploaded media type is not supported", response.getBody().detail());
    }

    @Test
    void handleNotFoundException_thenReturn404() {
        NotFoundException ex = new NotFoundException("Media 1 is not found");
        WebRequest request = mockWebRequest("/medias/1");

        ResponseEntity<ErrorVm> response = controllerAdvisor.handleNotFoundException(ex, request);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Media 1 is not found", response.getBody().detail());
    }

    @Test
    void handleConstraintViolation_thenReturn400() {
        ConstraintViolation<?> violation = mock(ConstraintViolation.class);
        when(violation.getRootBeanClass()).thenAnswer(inv -> String.class);
        when(violation.getPropertyPath()).thenReturn(PathImpl.createRootPath());
        when(violation.getMessage()).thenReturn("must not be null");

        ConstraintViolationException ex = new ConstraintViolationException("Constraint violation", Set.of(violation));

        ResponseEntity<ErrorVm> response = controllerAdvisor.handleConstraintViolation(ex);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Request information is not valid", response.getBody().detail());
        assertNotNull(response.getBody().fieldErrors());
        assertEquals(1, response.getBody().fieldErrors().size());
    }

    @Test
    void handleRuntimeException_thenReturn500() {
        RuntimeException ex = new RuntimeException("Unexpected error");
        WebRequest request = mockWebRequest("/medias");

        ResponseEntity<ErrorVm> response = controllerAdvisor.handleIoException(ex, request);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Unexpected error", response.getBody().detail());
    }

    @Test
    void handleOtherException_thenReturn500() {
        Exception ex = new Exception("Some exception");
        WebRequest request = mockWebRequest("/medias");

        ResponseEntity<ErrorVm> response = controllerAdvisor.handleOtherException(ex, request);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Some exception", response.getBody().detail());
    }
}
