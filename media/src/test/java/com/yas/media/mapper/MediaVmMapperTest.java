package com.yas.media.mapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import com.yas.media.model.Media;
import com.yas.media.viewmodel.MediaVm;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

class MediaVmMapperTest {

    private final MediaVmMapper mapper = Mappers.getMapper(MediaVmMapper.class);

    // ──────────────────────────────────────────────────────────────
    //  toVm — covers null-check branch in MapStruct generated impl
    // ──────────────────────────────────────────────────────────────

    @Test
    void toVm_whenMediaIsNull_thenReturnNull() {
        // Covers: if (media == null) return null; → TRUE branch
        assertNull(mapper.toVm(null));
    }

    @Test
    void toVm_whenMediaIsValid_thenMapAllFields() {
        // Covers: if (media == null) return null; → FALSE branch + field mapping
        Media media = new Media();
        media.setId(1L);
        media.setCaption("caption");
        media.setFileName("photo.png");
        media.setMediaType("image/png");
        media.setFilePath("/storage/photo.png");

        MediaVm vm = mapper.toVm(media);

        assertNotNull(vm);
        assertEquals(1L, vm.getId());
        assertEquals("caption", vm.getCaption());
        assertEquals("photo.png", vm.getFileName());
        assertEquals("image/png", vm.getMediaType());
    }

    // ──────────────────────────────────────────────────────────────
    //  toModel — covers the reverse mapping null-check branch
    // ──────────────────────────────────────────────────────────────

    @Test
    void toModel_whenMediaVmIsNull_thenReturnNull() {
        // Covers: if (mediaVm == null) return null; → TRUE branch
        assertNull(mapper.toModel(null));
    }

    @Test
    void toModel_whenMediaVmIsValid_thenMapFields() {
        // Covers: if (mediaVm == null) return null; → FALSE branch + field mapping
        MediaVm vm = new MediaVm(2L, "cap", "file.jpg", "image/jpeg", "http://url");

        Media media = mapper.toModel(vm);

        assertNotNull(media);
        assertEquals("cap", media.getCaption());
        assertEquals("file.jpg", media.getFileName());
        assertEquals("image/jpeg", media.getMediaType());
    }

    @Test
    void partialUpdate_whenVmIsNull_thenDoNothing() {
        Media media = new Media();
        media.setCaption("old");
        mapper.partialUpdate(media, null);
        assertEquals("old", media.getCaption());
    }

    @Test
    void partialUpdate_whenVmIsValid_thenUpdateFields() {
        Media media = new Media();
        media.setCaption("old cap");
        media.setFileName("old.jpg");
        media.setId(1L);
        media.setMediaType("image/jpeg");

        MediaVm vm = new MediaVm(2L, "new cap", "new.png", "image/png", null);
        mapper.partialUpdate(media, vm);

        assertEquals("new cap", media.getCaption());
        assertEquals("new.png", media.getFileName());
        assertEquals(2L, media.getId());
        assertEquals("image/png", media.getMediaType());
    }

    @Test
    void partialUpdate_whenVmFieldsAreNull_thenDoNotUpdate() {
        Media media = new Media();
        media.setCaption("old cap");
        media.setFileName("old.jpg");
        media.setId(1L);
        media.setMediaType("image/jpeg");

        MediaVm vm = new MediaVm(null, null, null, null, null);
        mapper.partialUpdate(media, vm);

        assertEquals("old cap", media.getCaption());
        assertEquals("old.jpg", media.getFileName());
        assertEquals(1L, media.getId());
        assertEquals("image/jpeg", media.getMediaType());
    }
}
