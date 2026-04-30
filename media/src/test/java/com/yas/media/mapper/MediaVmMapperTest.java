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

    @Test
    void toVm_whenMediaIsValid_thenReturnMediaVm() {
        Media media = new Media();
        media.setId(1L);
        media.setCaption("Caption");
        media.setFileName("file.png");
        media.setMediaType("image/png");

        MediaVm vm = mapper.toVm(media);

        assertNotNull(vm);
        assertEquals(media.getId(), vm.getId());
        assertEquals(media.getCaption(), vm.getCaption());
        assertEquals(media.getFileName(), vm.getFileName());
        assertEquals(media.getMediaType(), vm.getMediaType());
    }

    @Test
    void toVm_whenMediaIsNull_thenReturnNull() {
        assertNull(mapper.toVm(null));
    }

    @Test
    void toModel_whenVmIsValid_thenReturnMedia() {
        MediaVm vm = new MediaVm(1L, "Caption", "file.png", "image/png", "http://url");

        Media media = mapper.toModel(vm);

        assertNotNull(media);
        assertEquals(vm.getId(), media.getId());
        assertEquals(vm.getCaption(), media.getCaption());
        assertEquals(vm.getFileName(), media.getFileName());
        assertEquals(vm.getMediaType(), media.getMediaType());
    }

    @Test
    void toModel_whenVmIsNull_thenReturnNull() {
        assertNull(mapper.toModel(null));
    }

    @Test
    void partialUpdate_whenVmIsValid_thenUpdateModel() {
        Media media = new Media();
        media.setId(1L);
        media.setCaption("Old Caption");

        MediaVm vm = new MediaVm(1L, "New Caption", "file.png", "image/png", "http://url");

        mapper.partialUpdate(media, vm);

        assertEquals("New Caption", media.getCaption());
        assertEquals("file.png", media.getFileName());
        assertEquals("image/png", media.getMediaType());
    }
}
