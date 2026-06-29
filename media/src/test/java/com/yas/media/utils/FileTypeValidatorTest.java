package com.yas.media.utils;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import jakarta.validation.ConstraintValidatorContext;
import jakarta.validation.ConstraintValidatorContext.ConstraintViolationBuilder;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import javax.imageio.ImageIO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

class FileTypeValidatorTest {

    private FileTypeValidator validator;
    private ConstraintValidatorContext context;
    private ConstraintViolationBuilder violationBuilder;

    @BeforeEach
    void setUp() {
        validator = new FileTypeValidator();

        // Mock the annotation
        ValidFileType annotation = mock(ValidFileType.class);
        when(annotation.allowedTypes()).thenReturn(new String[]{"image/jpeg", "image/png", "image/gif"});
        when(annotation.message()).thenReturn("File type not allowed");
        validator.initialize(annotation);

        // Mock context
        context = mock(ConstraintValidatorContext.class);
        violationBuilder = mock(ConstraintViolationBuilder.class);
        when(context.buildConstraintViolationWithTemplate(anyString())).thenReturn(violationBuilder);
        when(violationBuilder.addConstraintViolation()).thenReturn(context);
    }

    /**
     * Creates a valid 1x1 image byte array for the given format (e.g. "png", "jpeg", "gif").
     * Uses BufferedImage + ImageIO.write to guarantee the bytes are always parseable by ImageIO.read.
     */
    private byte[] createValidImageBytes(String format) throws IOException {
        BufferedImage image = new BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB);
        image.setRGB(0, 0, Color.RED.getRGB());
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(image, format, baos);
        return baos.toByteArray();
    }

    @Test
    void isValid_whenFileIsNull_thenReturnFalse() {
        boolean result = validator.isValid(null, context);
        assertFalse(result);
    }

    @Test
    void isValid_whenContentTypeIsNull_thenReturnFalse() {
        MultipartFile file = new MockMultipartFile("file", "test.png", null, new byte[0]);
        boolean result = validator.isValid(file, context);
        assertFalse(result);
    }

    @Test
    void isValid_whenContentTypeNotAllowed_thenReturnFalse() {
        MultipartFile file = new MockMultipartFile("file", "test.pdf", "application/pdf", new byte[10]);
        boolean result = validator.isValid(file, context);
        assertFalse(result);
    }

    @Test
    void isValid_whenContentTypeAllowedButInvalidImageBytes_thenReturnFalse() {
        // "not-an-image" text → ImageIO.read returns null
        byte[] invalidImageBytes = "not-an-image".getBytes();
        MultipartFile file = new MockMultipartFile("file", "test.png", "image/png", invalidImageBytes);
        boolean result = validator.isValid(file, context);
        assertFalse(result);
    }

    @Test
    void isValid_whenValidPngFile_thenReturnTrue() throws IOException {
        // Programmatically generated PNG bytes – guaranteed valid
        byte[] validPngBytes = createValidImageBytes("png");
        MultipartFile file = new MockMultipartFile("file", "test.png", "image/png", validPngBytes);
        boolean result = validator.isValid(file, context);
        assertTrue(result);
    }

    @Test
    void isValid_whenValidJpegFile_thenReturnTrue() throws IOException {
        // Programmatically generated JPEG bytes – guaranteed valid
        byte[] validJpegBytes = createValidImageBytes("jpeg");
        MultipartFile file = new MockMultipartFile("file", "test.jpg", "image/jpeg", validJpegBytes);
        boolean result = validator.isValid(file, context);
        assertTrue(result);
    }

    @Test
    void isValid_whenValidGifFile_thenReturnTrue() throws IOException {
        // Programmatically generated GIF bytes – guaranteed valid
        byte[] validGifBytes = createValidImageBytes("gif");
        MultipartFile file = new MockMultipartFile("file", "test.gif", "image/gif", validGifBytes);
        boolean result = validator.isValid(file, context);
        assertTrue(result);
    }

    @Test
    void isValid_whenIoExceptionOnRead_thenReturnFalse() throws IOException {
        // Use a MultipartFile whose getInputStream() throws IOException
        MultipartFile brokenFile = mock(MultipartFile.class);
        when(brokenFile.getContentType()).thenReturn("image/png");
        when(brokenFile.getInputStream()).thenThrow(new IOException("Simulated IO error"));

        boolean result = validator.isValid(brokenFile, context);
        assertFalse(result);
    }
}
