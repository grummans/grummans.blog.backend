package com.grummans.noyblog.configuration;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("PageResponse Tests")
class PageResponseTest {

    @Test
    @DisplayName("Should create PageResponse from Page object")
    void shouldCreatePageResponseFromPageObject() {
        // Given
        List<String> content = Arrays.asList("Item 1", "Item 2", "Item 3");
        PageRequest pageRequest = PageRequest.of(0, 10);
        Page<String> page = new PageImpl<>(content, pageRequest, 25);

        // When
        PageResponse<String> response = new PageResponse<>(page);

        // Then
        assertThat(response.getContent()).hasSize(3);
        assertThat(response.getPage()).isEqualTo(1); // 0-based to 1-based
        assertThat(response.getSize()).isEqualTo(10);
        assertThat(response.getTotalElements()).isEqualTo(25);
        assertThat(response.getTotalPages()).isEqualTo(3);
    }

    @Test
    @DisplayName("Should handle empty page")
    void shouldHandleEmptyPage() {
        // Given
        List<String> content = Collections.emptyList();
        PageRequest pageRequest = PageRequest.of(0, 10);
        Page<String> page = new PageImpl<>(content, pageRequest, 0);

        // When
        PageResponse<String> response = new PageResponse<>(page);

        // Then
        assertThat(response.getContent()).isEmpty();
        assertThat(response.getPage()).isEqualTo(1);
        assertThat(response.getSize()).isEqualTo(10);
        assertThat(response.getTotalElements()).isZero();
        assertThat(response.getTotalPages()).isZero();
    }

    @Test
    @DisplayName("Should handle single page")
    void shouldHandleSinglePage() {
        // Given
        List<Integer> content = Arrays.asList(1, 2, 3, 4, 5);
        PageRequest pageRequest = PageRequest.of(0, 10);
        Page<Integer> page = new PageImpl<>(content, pageRequest, 5);

        // When
        PageResponse<Integer> response = new PageResponse<>(page);

        // Then
        assertThat(response.getContent()).hasSize(5);
        assertThat(response.getTotalPages()).isEqualTo(1);
    }

    @Test
    @DisplayName("Should handle middle page correctly")
    void shouldHandleMiddlePageCorrectly() {
        // Given
        List<String> content = Arrays.asList("Item 11", "Item 12");
        PageRequest pageRequest = PageRequest.of(1, 10); // Second page (0-indexed)
        Page<String> page = new PageImpl<>(content, pageRequest, 22);

        // When
        PageResponse<String> response = new PageResponse<>(page);

        // Then
        assertThat(response.getPage()).isEqualTo(2); // 1-based page number
        assertThat(response.getTotalPages()).isEqualTo(3);
    }

    @Test
    @DisplayName("Should work with different data types")
    void shouldWorkWithDifferentDataTypes() {
        // Given
        List<Double> content = Arrays.asList(1.5, 2.5, 3.5);
        PageRequest pageRequest = PageRequest.of(0, 5);
        Page<Double> page = new PageImpl<>(content, pageRequest, 3);

        // When
        PageResponse<Double> response = new PageResponse<>(page);

        // Then
        assertThat(response.getContent()).containsExactly(1.5, 2.5, 3.5);
    }
}

