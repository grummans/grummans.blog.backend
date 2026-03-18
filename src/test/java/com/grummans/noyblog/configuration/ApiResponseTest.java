package com.grummans.noyblog.configuration;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("ApiResponse Tests")
class ApiResponseTest {

    @Test
    @DisplayName("Should create ApiResponse with all fields")
    void shouldCreateApiResponseWithAllFields() {
        // Given
        ApiResponse<String> response = new ApiResponse<>();

        // When
        response.setCode(200);
        response.setMessage("Success");
        response.setData("Test data");

        // Then
        assertThat(response.getCode()).isEqualTo(200);
        assertThat(response.getMessage()).isEqualTo("Success");
        assertThat(response.getData()).isEqualTo("Test data");
    }

    @Test
    @DisplayName("Should handle null data")
    void shouldHandleNullData() {
        // Given
        ApiResponse<String> response = new ApiResponse<>();

        // When
        response.setCode(404);
        response.setMessage("Not found");
        response.setData(null);

        // Then
        assertThat(response.getCode()).isEqualTo(404);
        assertThat(response.getMessage()).isEqualTo("Not found");
        assertThat(response.getData()).isNull();
    }

    @Test
    @DisplayName("Should work with different data types")
    void shouldWorkWithDifferentDataTypes() {
        // Given
        ApiResponse<Integer> intResponse = new ApiResponse<>();
        ApiResponse<Boolean> boolResponse = new ApiResponse<>();

        // When
        intResponse.setData(42);
        boolResponse.setData(true);

        // Then
        assertThat(intResponse.getData()).isEqualTo(42);
        assertThat(boolResponse.getData()).isTrue();
    }

    @Test
    @DisplayName("Should set and get code correctly")
    void shouldSetAndGetCodeCorrectly() {
        // Given
        ApiResponse<Object> response = new ApiResponse<>();

        // When
        response.setCode(500);

        // Then
        assertThat(response.getCode()).isEqualTo(500);
    }

    @Test
    @DisplayName("Should set and get message correctly")
    void shouldSetAndGetMessageCorrectly() {
        // Given
        ApiResponse<Object> response = new ApiResponse<>();

        // When
        response.setMessage("Error occurred");

        // Then
        assertThat(response.getMessage()).isEqualTo("Error occurred");
    }
}

