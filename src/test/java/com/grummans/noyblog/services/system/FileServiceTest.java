package com.grummans.noyblog.services.system;

import com.grummans.noyblog.exceptions.FileUploadException;
import com.grummans.noyblog.model.PostAttachments;
import com.grummans.noyblog.repository.PostAttachmentRepository;
import io.minio.MinioClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("FileService Tests")
class FileServiceTest {

    @SuppressWarnings("unused") // Required for @InjectMocks to work
    @Mock
    private MinioClient minioClient;

    @Mock
    private PostAttachmentRepository postAttachmentRepository;

    @InjectMocks
    private FileService fileService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(fileService, "minioEndpoint", "https://minioconsole.grummans.me");
    }

    @Nested
    @DisplayName("extractFileUrlsFromContent Tests")
    class ExtractFileUrlsFromContentTests {

        @Test
        @DisplayName("Should extract image URLs from HTML content")
        void shouldExtractImageUrlsFromHtmlContent() {
            // Given
            String htmlContent = """
                <p>Some text</p>
                <img src="https://minioconsole.grummans.me/posts/1/content/image1.jpg" />
                <p>More text</p>
                <img src="https://minioconsole.grummans.me/posts/1/content/image2.png" />
                """;

            // When
            List<String> result = fileService.extractFileUrlsFromContent(htmlContent);

            // Then
            assertThat(result)
                    .hasSize(2)
                    .contains(
                            "https://minioconsole.grummans.me/posts/1/content/image1.jpg",
                            "https://minioconsole.grummans.me/posts/1/content/image2.png"
                    );
        }

        @Test
        @DisplayName("Should return empty list for content without images")
        void shouldReturnEmptyListForContentWithoutImages() {
            // Given
            String htmlContent = "<p>Some text without images</p>";

            // When
            List<String> result = fileService.extractFileUrlsFromContent(htmlContent);

            // Then
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("Should return empty list for null content")
        void shouldReturnEmptyListForNullContent() {
            // When
            List<String> result = fileService.extractFileUrlsFromContent(null);

            // Then
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("Should return empty list for empty content")
        void shouldReturnEmptyListForEmptyContent() {
            // When
            List<String> result = fileService.extractFileUrlsFromContent("");

            // Then
            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("updateContentUrls Tests")
    class UpdateContentUrlsTests {

        @Test
        @DisplayName("Should update URLs in HTML content")
        void shouldUpdateUrlsInHtmlContent() {
            // Given
            String htmlContent = """
                <p>Text</p>
                <img src="https://minioconsole.grummans.me/posts/temp/content-files/image1.jpg" />
                """;
            Map<String, String> urlMapping = Map.of(
                    "https://minioconsole.grummans.me/posts/temp/content-files/image1.jpg",
                    "https://minioconsole.grummans.me/posts/1/content/image1.jpg"
            );

            // When
            String result = fileService.updateContentUrls(htmlContent, urlMapping);

            // Then
            assertThat(result)
                    .contains("https://minioconsole.grummans.me/posts/1/content/image1.jpg")
                    .doesNotContain("temp/content-files");
        }

        @Test
        @DisplayName("Should return original content when URL mapping is empty")
        void shouldReturnOriginalContentWhenUrlMappingIsEmpty() {
            // Given
            String htmlContent = "<p>Some content</p>";
            Map<String, String> urlMapping = Map.of();

            // When
            String result = fileService.updateContentUrls(htmlContent, urlMapping);

            // Then
            assertThat(result).isEqualTo(htmlContent);
        }

        @Test
        @DisplayName("Should return original content when content is null")
        void shouldReturnNullWhenContentIsNull() {
            // When
            String result = fileService.updateContentUrls(null, Map.of("old", "new"));

            // Then
            assertThat(result).isNull();
        }

        @Test
        @DisplayName("Should return original content when content is empty")
        void shouldReturnOriginalContentWhenContentIsEmpty() {
            // When
            String result = fileService.updateContentUrls("", Map.of("old", "new"));

            // Then
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("Should return original content when URL mapping is null")
        void shouldReturnOriginalContentWhenUrlMappingIsNull() {
            // Given
            String htmlContent = "<p>Some content</p>";

            // When
            String result = fileService.updateContentUrls(htmlContent, null);

            // Then
            assertThat(result).isEqualTo(htmlContent);
        }
    }

    @Nested
    @DisplayName("getPostAttachments Tests")
    class GetPostAttachmentsTests {

        @Test
        @DisplayName("Should return attachments for post")
        void shouldReturnAttachmentsForPost() {
            // Given
            PostAttachments attachment = new PostAttachments();
            attachment.setId(1);
            attachment.setPostId(1);
            attachment.setOriginalFilename("test.pdf");

            when(postAttachmentRepository.findByPostId(1)).thenReturn(List.of(attachment));

            // When
            List<PostAttachments> result = fileService.getPostAttachments(1);

            // Then
            assertThat(result).hasSize(1);
            assertThat(result.getFirst().getOriginalFilename()).isEqualTo("test.pdf");
            verify(postAttachmentRepository).findByPostId(1);
        }

        @Test
        @DisplayName("Should return empty list when no attachments")
        void shouldReturnEmptyListWhenNoAttachments() {
            // Given
            when(postAttachmentRepository.findByPostId(999)).thenReturn(List.of());

            // When
            List<PostAttachments> result = fileService.getPostAttachments(999);

            // Then
            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("File Validation Tests")
    class FileValidationTests {

        @Test
        @DisplayName("Should throw exception for empty file")
        void shouldThrowExceptionForEmptyFile() {
            // Given
            MultipartFile emptyFile = new MockMultipartFile(
                    "file", "test.jpg", "image/jpeg", new byte[0]
            );

            // When/Then
            assertThatThrownBy(() -> fileService.uploadContentFile(emptyFile))
                    .isInstanceOf(FileUploadException.class)
                    .hasMessageContaining("empty");
        }

        @Test
        @DisplayName("Should throw exception for file without extension")
        void shouldThrowExceptionForFileWithoutExtension() {
            // Given
            MultipartFile noExtFile = new MockMultipartFile(
                    "file", "testfile", "application/octet-stream", "test content".getBytes()
            );

            // When/Then
            assertThatThrownBy(() -> fileService.uploadContentFile(noExtFile))
                    .isInstanceOf(FileUploadException.class)
                    .hasMessageContaining("no extension");
        }

        @Test
        @DisplayName("Should throw exception for unsupported file type")
        void shouldThrowExceptionForUnsupportedFileType() {
            // Given
            MultipartFile unsupportedFile = new MockMultipartFile(
                    "file", "test.exe", "application/x-executable", "test content".getBytes()
            );

            // When/Then
            assertThatThrownBy(() -> fileService.uploadContentFile(unsupportedFile))
                    .isInstanceOf(FileUploadException.class)
                    .hasMessageContaining("not supported");
        }

        @Test
        @DisplayName("Should throw exception for file with null filename")
        void shouldThrowExceptionForFileWithNullFilename() {
            // Given
            MultipartFile nullNameFile = new MockMultipartFile(
                    "file", null, "image/jpeg", "test content".getBytes()
            );

            // When/Then
            assertThatThrownBy(() -> fileService.uploadContentFile(nullNameFile))
                    .isInstanceOf(FileUploadException.class);
        }

        @Test
        @DisplayName("Should throw exception for file with empty filename")
        void shouldThrowExceptionForFileWithEmptyFilename() {
            // Given
            MultipartFile emptyNameFile = new MockMultipartFile(
                    "file", "", "image/jpeg", "test content".getBytes()
            );

            // When/Then
            assertThatThrownBy(() -> fileService.uploadContentFile(emptyNameFile))
                    .isInstanceOf(FileUploadException.class);
        }

        @Test
        @DisplayName("Should throw exception when image file exceeds max size")
        void shouldThrowExceptionWhenImageFileExceedsMaxSize() {
            // Given - Create a file larger than 10MB
            byte[] largeContent = new byte[11 * 1024 * 1024]; // 11 MB
            MultipartFile largeImageFile = new MockMultipartFile(
                    "file", "large.jpg", "image/jpeg", largeContent
            );

            // When/Then
            assertThatThrownBy(() -> fileService.uploadContentFile(largeImageFile))
                    .isInstanceOf(FileUploadException.class)
                    .hasMessageContaining("exceeds maximum");
        }
    }

    @Nested
    @DisplayName("deleteAttachment Tests")
    class DeleteAttachmentTests {

        @Test
        @DisplayName("Should throw exception when attachment not found")
        void shouldThrowExceptionWhenAttachmentNotFound() {
            // Given
            when(postAttachmentRepository.findById(999)).thenReturn(Optional.empty());

            // When/Then
            assertThatThrownBy(() -> fileService.deleteAttachment(1, 999))
                    .isInstanceOf(FileUploadException.class)
                    .hasMessageContaining("not found");
        }

        @Test
        @DisplayName("Should throw exception when attachment does not belong to post")
        void shouldThrowExceptionWhenAttachmentNotBelongToPost() {
            // Given
            PostAttachments attachment = new PostAttachments();
            attachment.setId(1);
            attachment.setPostId(2); // Different post ID

            when(postAttachmentRepository.findById(1)).thenReturn(Optional.of(attachment));

            // When/Then
            assertThatThrownBy(() -> fileService.deleteAttachment(1, 1))
                    .isInstanceOf(FileUploadException.class)
                    .hasMessageContaining("does not belong to post");
        }
    }

    @Nested
    @DisplayName("moveContentFilesToPost Tests")
    class MoveContentFilesToPostTests {

        @Test
        @DisplayName("Should return empty map for null file URLs")
        void shouldReturnEmptyMapForNullFileUrls() {
            // When
            Map<String, String> result = fileService.moveContentFilesToPost(1, null);

            // Then
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("Should return empty map for empty file URLs list")
        void shouldReturnEmptyMapForEmptyFileUrlsList() {
            // When
            Map<String, String> result = fileService.moveContentFilesToPost(1, List.of());

            // Then
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("Should skip non-temp file URLs")
        void shouldSkipNonTempFileUrls() {
            // Given
            List<String> fileUrls = List.of(
                    "https://minioconsole.grummans.me/posts/1/content/existing.jpg"
            );

            // When
            Map<String, String> result = fileService.moveContentFilesToPost(1, fileUrls);

            // Then
            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("deleteFileByUrl Tests")
    class DeleteFileByUrlTests {

        @Test
        @DisplayName("Should not throw when URL is null")
        void shouldNotThrowWhenUrlIsNull() {
            // When/Then - should not throw
            assertThatCode(() -> fileService.deleteFileByUrl(null))
                    .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("Should not throw when URL is empty")
        void shouldNotThrowWhenUrlIsEmpty() {
            // When/Then - should not throw
            assertThatCode(() -> fileService.deleteFileByUrl(""))
                    .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("Should not throw when URL is whitespace only")
        void shouldNotThrowWhenUrlIsWhitespaceOnly() {
            // When/Then - should not throw
            assertThatCode(() -> fileService.deleteFileByUrl("   "))
                    .doesNotThrowAnyException();
        }
    }
}

