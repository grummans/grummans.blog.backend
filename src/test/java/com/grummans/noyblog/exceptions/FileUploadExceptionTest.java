package com.grummans.noyblog.exceptions;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("FileUploadException Tests")
class FileUploadExceptionTest {

    @Test
    @DisplayName("Should create exception with message")
    void shouldCreateExceptionWithMessage() {
        // Given
        String message = "File upload failed";

        // When
        FileUploadException exception = new FileUploadException(message);

        // Then
        assertThat(exception.getMessage()).isEqualTo("File upload failed");
        assertThat(exception).isInstanceOf(RuntimeException.class);
    }

    @Test
    @DisplayName("Should create exception with message and cause")
    void shouldCreateExceptionWithMessageAndCause() {
        // Given
        String message = "File upload failed";
        Throwable cause = new RuntimeException("Original error");

        // When
        FileUploadException exception = new FileUploadException(message, cause);

        // Then
        assertThat(exception.getMessage()).isEqualTo("File upload failed");
        assertThat(exception.getCause()).isEqualTo(cause);
        assertThat(exception.getCause().getMessage()).isEqualTo("Original error");
    }

    @Test
    @DisplayName("Should be throwable")
    void shouldBeThrowable() {
        // Given
        FileUploadException exception = new FileUploadException("Test error");

        // When/Then
        try {
            throw exception;
        } catch (FileUploadException e) {
            assertThat(e.getMessage()).isEqualTo("Test error");
        }
    }
}

