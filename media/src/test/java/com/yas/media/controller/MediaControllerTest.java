package com.yas.media.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.yas.commonlibrary.exception.NotFoundException;
import com.yas.media.exception.ControllerAdvisor;
import com.yas.media.model.Media;
import com.yas.media.model.dto.MediaDto;
import com.yas.media.service.MediaService;
import com.yas.media.viewmodel.MediaVm;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import javax.imageio.ImageIO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

@ExtendWith(MockitoExtension.class)
class MediaControllerTest {

    @Mock
    private MediaService mediaService;

    @InjectMocks
    private MediaController mediaController;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
            .standaloneSetup(mediaController)
            .setControllerAdvice(new ControllerAdvisor())
            .build();
    }

    /**
     * Creates a valid 1x1 image byte array for the given format via BufferedImage.
     * This guarantees the bytes are always parseable by ImageIO.read.
     */
    private byte[] createValidImageBytes(String format) throws IOException {
        BufferedImage image = new BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB);
        image.setRGB(0, 0, Color.RED.getRGB());
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(image, format, baos);
        return baos.toByteArray();
    }

    // ==============================
    // POST /medias - create
    // ==============================

    @Test
    void create_whenValidFile_thenReturn200() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
            "multipartFile", "test.png", MediaType.IMAGE_PNG_VALUE, createValidImageBytes("png")
        );

        Media savedMedia = new Media();
        savedMedia.setId(1L);
        savedMedia.setCaption("caption");
        savedMedia.setFileName("test.png");
        savedMedia.setMediaType(MediaType.IMAGE_PNG_VALUE);

        when(mediaService.saveMedia(any())).thenReturn(savedMedia);

        mockMvc.perform(multipart("/medias")
                .file(file)
                .param("caption", "caption")
                .param("fileNameOverride", "test.png"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(1L))
            .andExpect(jsonPath("$.caption").value("caption"))
            .andExpect(jsonPath("$.fileName").value("test.png"));
    }

    @Test
    void create_whenValidFileNoOverride_thenReturn200() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
            "multipartFile", "test.png", MediaType.IMAGE_PNG_VALUE, createValidImageBytes("png")
        );

        Media savedMedia = new Media();
        savedMedia.setId(2L);
        savedMedia.setCaption("caption");
        savedMedia.setFileName("test.png");
        savedMedia.setMediaType(MediaType.IMAGE_PNG_VALUE);

        when(mediaService.saveMedia(any())).thenReturn(savedMedia);

        mockMvc.perform(multipart("/medias")
                .file(file)
                .param("caption", "caption"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(2L))
            .andExpect(jsonPath("$.caption").value("caption"))
            .andExpect(jsonPath("$.fileName").value("test.png"));
    }

    @Test
    void create_whenInvalidImageContent_thenReturn400() throws Exception {
        // content-type is image/png but bytes are not a valid image → FileTypeValidator returns false → 400
        MockMultipartFile invalidFile = new MockMultipartFile(
            "multipartFile", "test.png", MediaType.IMAGE_PNG_VALUE, "not-an-image".getBytes()
        );

        mockMvc.perform(multipart("/medias")
                .file(invalidFile)
                .param("caption", "caption"))
            .andExpect(status().isBadRequest());
    }

    // ==============================
    // DELETE /medias/{id}
    // ==============================

    @Test
    void delete_whenValidId_thenReturn204() throws Exception {
        doNothing().when(mediaService).removeMedia(1L);

        mockMvc.perform(delete("/medias/1"))
            .andExpect(status().isNoContent());
    }

    @Test
    void delete_whenMediaNotFound_thenReturn404() throws Exception {
        doThrow(new NotFoundException("Media 1 is not found"))
            .when(mediaService).removeMedia(1L);

        mockMvc.perform(delete("/medias/1"))
            .andExpect(status().isNotFound());
    }

    // ==============================
    // GET /medias/{id}
    // ==============================

    @Test
    void get_whenMediaExists_thenReturn200() throws Exception {
        MediaVm mediaVm = new MediaVm(1L, "caption", "test.png", MediaType.IMAGE_PNG_VALUE, "http://url");
        when(mediaService.getMediaById(1L)).thenReturn(mediaVm);

        mockMvc.perform(get("/medias/1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(1L))
            .andExpect(jsonPath("$.caption").value("caption"))
            .andExpect(jsonPath("$.fileName").value("test.png"));
    }

    @Test
    void get_whenMediaNotFound_thenReturn404() throws Exception {
        when(mediaService.getMediaById(1L)).thenReturn(null);

        mockMvc.perform(get("/medias/1"))
            .andExpect(status().isNotFound());
    }

    // ==============================
    // GET /medias?ids=...
    // ==============================

    @Test
    void getByIds_whenMediasExist_thenReturn200() throws Exception {
        MediaVm mediaVm1 = new MediaVm(1L, "cap1", "file1.png", MediaType.IMAGE_PNG_VALUE, "http://url1");
        MediaVm mediaVm2 = new MediaVm(2L, "cap2", "file2.png", MediaType.IMAGE_PNG_VALUE, "http://url2");

        when(mediaService.getMediaByIds(List.of(1L, 2L))).thenReturn(List.of(mediaVm1, mediaVm2));

        mockMvc.perform(get("/medias").param("ids", "1", "2"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void getByIds_whenNoMediaFound_thenReturn404() throws Exception {
        when(mediaService.getMediaByIds(anyList())).thenReturn(List.of());

        mockMvc.perform(get("/medias").param("ids", "99"))
            .andExpect(status().isNotFound());
    }

    @Test
    void getByIds_whenServiceReturnsNull_thenReturn404() throws Exception {
        when(mediaService.getMediaByIds(anyList())).thenReturn(null);

        mockMvc.perform(get("/medias").param("ids", "99"))
            .andExpect(status().isNotFound());
    }

    // ==============================
    // GET /medias/{id}/file/{fileName}
    // ==============================

    @Test
    void getFile_whenMediaExists_thenReturn200WithContent() throws Exception {
        byte[] fileContent = "image-data".getBytes();
        MediaDto mediaDto = MediaDto.builder()
            .content(new ByteArrayInputStream(fileContent))
            .mediaType(MediaType.IMAGE_PNG)
            .build();

        when(mediaService.getFile(1L, "test.png")).thenReturn(mediaDto);

        mockMvc.perform(get("/medias/1/file/test.png"))
            .andExpect(status().isOk())
            .andExpect(header().string(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"test.png\""));
    }

    @Test
    void getFile_whenMediaContentIsNull_thenReturn500() throws Exception {
        MediaDto mediaDto = MediaDto.builder().build(); // content is null
        when(mediaService.getFile(1L, "test.png")).thenReturn(mediaDto);

        mockMvc.perform(get("/medias/1/file/test.png"))
            .andExpect(status().isInternalServerError());
    }
}
