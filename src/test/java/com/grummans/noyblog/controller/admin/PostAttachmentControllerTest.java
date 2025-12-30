package com.grummans.noyblog.controller.admin;

import com.grummans.noyblog.model.PostAttachments;
import com.grummans.noyblog.services.system.FileService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(
        controllers = PostAttachmentController.class,
        excludeAutoConfiguration = {SecurityAutoConfiguration.class},
        excludeFilters = @ComponentScan.Filter(type = FilterType.REGEX, pattern = "com.grummans.noyblog.configuration.*")
)
@DisplayName("PostAttachmentController Tests")
class PostAttachmentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private FileService fileService;

    private PostAttachments testAttachment;

    @BeforeEach
    void setUp() {
        testAttachment = new PostAttachments();
        testAttachment.setId(1);
        testAttachment.setPostId(1);
        testAttachment.setOriginalFilename("test.pdf");
        testAttachment.setStoredFilename("uuid.pdf");
        testAttachment.setFileType("DOCUMENT");
        testAttachment.setMimeType("application/pdf");
        testAttachment.setFileSize(1024L);
        testAttachment.setStoragePath("1/attachments/uuid.pdf");
    }

    @Nested
    @DisplayName("POST /a/posts/{postId}/attachments Tests")
    class UploadAttachmentTests {

        @Test
        @DisplayName("Should upload attachment successfully")
        void shouldUploadAttachmentSuccessfully() throws Exception {
            // Given
            MockMultipartFile file = new MockMultipartFile(
                    "file", "test.pdf", "application/pdf", "pdf content".getBytes()
            );
            when(fileService.uploadPostAttachment(eq(1), any())).thenReturn(testAttachment);

            // When/Then
            mockMvc.perform(multipart("/a/posts/1/attachments")
                            .file(file))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code", is(201)))
                    .andExpect(jsonPath("$.message", containsString("uploaded")))
                    .andExpect(jsonPath("$.data.originalFilename", is("test.pdf")));

            verify(fileService).uploadPostAttachment(eq(1), any());
        }
    }

    @Nested
    @DisplayName("GET /a/posts/{postId}/attachments Tests")
    class GetAttachmentsTests {

        @Test
        @DisplayName("Should return attachments for post")
        void shouldReturnAttachmentsForPost() throws Exception {
            // Given
            when(fileService.getPostAttachments(1)).thenReturn(List.of(testAttachment));

            // When/Then
            mockMvc.perform(get("/a/posts/1/attachments"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code", is(200)))
                    .andExpect(jsonPath("$.data", hasSize(1)))
                    .andExpect(jsonPath("$.data[0].originalFilename", is("test.pdf")));

            verify(fileService).getPostAttachments(1);
        }

        @Test
        @DisplayName("Should return empty list when no attachments")
        void shouldReturnEmptyListWhenNoAttachments() throws Exception {
            // Given
            when(fileService.getPostAttachments(999)).thenReturn(List.of());

            // When/Then
            mockMvc.perform(get("/a/posts/999/attachments"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code", is(200)))
                    .andExpect(jsonPath("$.data", hasSize(0)));

            verify(fileService).getPostAttachments(999);
        }
    }

    @Nested
    @DisplayName("DELETE /a/posts/{postId}/attachments/{attachmentId} Tests")
    class DeleteAttachmentTests {

        @Test
        @DisplayName("Should delete attachment successfully")
        void shouldDeleteAttachmentSuccessfully() throws Exception {
            // Given
            doNothing().when(fileService).deleteAttachment(1, 1);

            // When/Then
            mockMvc.perform(delete("/a/posts/1/attachments/1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code", is(200)))
                    .andExpect(jsonPath("$.message", containsString("deleted")));

            verify(fileService).deleteAttachment(1, 1);
        }
    }
}

