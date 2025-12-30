package com.grummans.noyblog.controller.system.file;

import com.grummans.noyblog.services.system.FileService;
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

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(
        controllers = FileController.class,
        excludeAutoConfiguration = {SecurityAutoConfiguration.class},
        excludeFilters = @ComponentScan.Filter(type = FilterType.REGEX, pattern = "com.grummans.noyblog.configuration.*")
)
@DisplayName("FileController Tests")
class FileControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private FileService fileService;

    @Nested
    @DisplayName("POST /file/upload-content-file Tests")
    class UploadContentFileTests {

        @Test
        @DisplayName("Should upload content file successfully")
        void shouldUploadContentFileSuccessfully() throws Exception {
            // Given
            MockMultipartFile file = new MockMultipartFile(
                    "file", "test.jpg", "image/jpeg", "test image content".getBytes()
            );
            String expectedUrl = "https://minioconsole.grummans.me/posts/temp/content-files/uuid.jpg";
            when(fileService.uploadContentFile(any())).thenReturn(expectedUrl);

            // When/Then
            mockMvc.perform(multipart("/file/upload-content-file")
                            .file(file))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code", is(200)))
                    .andExpect(jsonPath("$.data", is(expectedUrl)))
                    .andExpect(jsonPath("$.message", containsString("successfully")));

            verify(fileService).uploadContentFile(any());
        }
    }

    @Nested
    @DisplayName("POST /file/upload-avatar Tests")
    class UploadAvatarTests {

        @Test
        @DisplayName("Should upload avatar successfully")
        void shouldUploadAvatarSuccessfully() throws Exception {
            // Given
            MockMultipartFile file = new MockMultipartFile(
                    "file", "avatar.png", "image/png", "avatar content".getBytes()
            );
            String expectedUrl = "https://minioconsole.grummans.me/users/1/avatar/uuid.png";
            when(fileService.uploadUserAvatar(eq(1), any())).thenReturn(expectedUrl);

            // When/Then
            mockMvc.perform(multipart("/file/upload-avatar")
                            .file(file)
                            .param("userId", "1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code", is(200)))
                    .andExpect(jsonPath("$.data", is(expectedUrl)))
                    .andExpect(jsonPath("$.message", containsString("Avatar")));

            verify(fileService).uploadUserAvatar(eq(1), any());
        }
    }

    @Nested
    @DisplayName("DELETE /file/{attachmentId} Tests")
    class DeleteAttachmentTests {

        @Test
        @DisplayName("Should delete attachment successfully")
        void shouldDeleteAttachmentSuccessfully() throws Exception {
            // Given
            doNothing().when(fileService).deleteAttachment(1);

            // When/Then
            mockMvc.perform(delete("/file/1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code", is(200)))
                    .andExpect(jsonPath("$.message", containsString("deleted")));

            verify(fileService).deleteAttachment(1);
        }
    }
}

