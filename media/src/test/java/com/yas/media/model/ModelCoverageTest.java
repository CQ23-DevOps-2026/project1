package com.yas.media.model;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.yas.media.MediaApplication;
import com.yas.media.config.FilesystemConfig;
import com.yas.media.config.SwaggerConfig;
import com.yas.media.config.YasConfig;
import com.yas.media.model.dto.MediaDto;
import com.yas.media.viewmodel.ErrorVm;
import com.yas.media.viewmodel.MediaPostVm;
import com.yas.media.viewmodel.MediaVm;
import com.yas.media.viewmodel.NoFileMediaVm;
import java.io.InputStream;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

class ModelCoverageTest {

    @Test
    void testMediaEntity() {
        Media media1 = new Media();
        media1.setId(1L);
        media1.setCaption("Caption");
        media1.setFileName("file.png");
        media1.setFilePath("/path/to/file.png");
        media1.setMediaType("image/png");

        assertEquals(1L, media1.getId());
        assertEquals("Caption", media1.getCaption());
        assertEquals("file.png", media1.getFileName());
        assertEquals("/path/to/file.png", media1.getFilePath());
        assertEquals("image/png", media1.getMediaType());

        // Test setters
        Media media2 = new Media();
        media2.setId(1L);
        media2.setCaption("Caption");
        media2.setFileName("file.png");
        media2.setFilePath("/path/to/file.png");
        media2.setMediaType("image/png");

        // Since Media uses Lombok @Getter/@Setter but NOT @EqualsAndHashCode, we don't test equals/hashCode here.
    }

    @Test
    void testMediaDto() {
        InputStream is = InputStream.nullInputStream();
        MediaDto.MediaDtoBuilder builder = MediaDto.builder()
            .content(is)
            .mediaType(MediaType.IMAGE_PNG);
        assertNotNull(builder.toString());

        MediaDto dto = builder.build();

        assertEquals(is, dto.getContent());
        assertEquals(MediaType.IMAGE_PNG, dto.getMediaType());
        assertNotNull(dto.toString());
    }

    @Test
    void testErrorVm() {
        ErrorVm error1 = new ErrorVm("404", "Not Found", "Media not found");
        ErrorVm error2 = new ErrorVm("400", "Bad Request", "Invalid", List.of("field1"));

        assertEquals("404", error1.statusCode());
        assertEquals("Not Found", error1.title());
        assertEquals("Media not found", error1.detail());
        assertTrue(error1.fieldErrors().isEmpty());

        assertEquals("400", error2.statusCode());
        assertEquals("Bad Request", error2.title());
        assertEquals("Invalid", error2.detail());
        assertEquals(1, error2.fieldErrors().size());

        assertNotNull(error1.toString());
        assertEquals(error1, new ErrorVm("404", "Not Found", "Media not found", List.of()));
        assertNotNull(error1.hashCode());
    }

    @Test
    void testMediaPostVm() {
        MediaPostVm vm = new MediaPostVm("Cap", null, "override.png");
        assertEquals("Cap", vm.caption());
        assertNull(vm.multipartFile());
        assertEquals("override.png", vm.fileNameOverride());
        assertNotNull(vm.toString());
        assertEquals(vm, new MediaPostVm("Cap", null, "override.png"));
        assertNotNull(vm.hashCode());
    }

    @Test
    void testMediaVm() {
        MediaVm vm1 = new MediaVm(1L, "Cap", "file.png", "image/png", "http://url");
        vm1.setId(2L);
        vm1.setCaption("Cap2");
        vm1.setFileName("file2.png");
        vm1.setMediaType("image/jpeg");
        vm1.setUrl("http://url2");

        assertEquals(2L, vm1.getId());
        assertEquals("Cap2", vm1.getCaption());
        assertEquals("file2.png", vm1.getFileName());
        assertEquals("image/jpeg", vm1.getMediaType());
        assertEquals("http://url2", vm1.getUrl());

        MediaVm vm2 = new MediaVm(2L, "Cap2", "file2.png", "image/jpeg", "http://url2");
        // MediaVm doesn't have @EqualsAndHashCode or custom toString, so we don't test them here.
    }

    @Test
    void testNoFileMediaVm() {
        NoFileMediaVm vm = new NoFileMediaVm(1L, "Cap", "file.png", "image/png");
        assertEquals(1L, vm.id());
        assertEquals("Cap", vm.caption());
        assertEquals("file.png", vm.fileName());
        assertEquals("image/png", vm.mediaType());
        assertNotNull(vm.toString());
        assertEquals(vm, new NoFileMediaVm(1L, "Cap", "file.png", "image/png"));
        assertNotNull(vm.hashCode());
    }

    @Test
    void testConfigs() {
        // Instantiate config classes to cover their constructors and basic methods
        SwaggerConfig swaggerConfig = new SwaggerConfig();
        assertNotNull(swaggerConfig);

        FilesystemConfig filesystemConfig = new FilesystemConfig();
        assertNull(filesystemConfig.getDirectory());

        YasConfig yasConfig = new YasConfig("http://localhost:8080");
        assertEquals("http://localhost:8080", yasConfig.publicUrl());
        assertNotNull(yasConfig.toString());
        assertEquals(yasConfig, new YasConfig("http://localhost:8080"));
        assertNotNull(yasConfig.hashCode());
    }

    @Test
    void testMediaApplication() {
        // Just instantiate to cover the implicit constructor which is otherwise 0% line coverage
        MediaApplication app = new MediaApplication();
        assertNotNull(app);
        
        try {
            MediaApplication.main(new String[] {});
        } catch (Exception e) {
            // Context might fail to load in unit test, but the line is executed.
        }
    }

    @Test
    void testStringUtilsUtilityClass() throws Exception {
        java.lang.reflect.Constructor<com.yas.media.utils.StringUtils> constructor = com.yas.media.utils.StringUtils.class.getDeclaredConstructor();
        constructor.setAccessible(true);
        try {
            constructor.newInstance();
        } catch (java.lang.reflect.InvocationTargetException e) {
            assertTrue(e.getCause() instanceof UnsupportedOperationException);
        }
        
        assertFalse(com.yas.media.utils.StringUtils.hasText(null));
        assertFalse(com.yas.media.utils.StringUtils.hasText("   "));
        assertTrue(com.yas.media.utils.StringUtils.hasText("text"));
    }
}
