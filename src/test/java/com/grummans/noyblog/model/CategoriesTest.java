package com.grummans.noyblog.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Categories Model Tests")
class CategoriesTest {

    @Test
    @DisplayName("Should create Categories using builder")
    void shouldCreateCategoriesUsingBuilder() {
        // When
        Categories category = Categories.builder()
                .id(1)
                .name("Technology")
                .slug("technology")
                .description("Tech articles")
                .color("#3498db")
                .build();

        // Then
        assertThat(category.getId()).isEqualTo(1);
        assertThat(category.getName()).isEqualTo("Technology");
        assertThat(category.getSlug()).isEqualTo("technology");
        assertThat(category.getDescription()).isEqualTo("Tech articles");
        assertThat(category.getColor()).isEqualTo("#3498db");
    }

    @Test
    @DisplayName("Should create Categories using no-args constructor")
    void shouldCreateCategoriesUsingNoArgsConstructor() {
        // When
        Categories category = new Categories();
        category.setId(2);
        category.setName("Lifestyle");
        category.setSlug("lifestyle");
        category.setDescription("Life articles");
        category.setColor("#e74c3c");

        // Then
        assertThat(category.getId()).isEqualTo(2);
        assertThat(category.getName()).isEqualTo("Lifestyle");
        assertThat(category.getSlug()).isEqualTo("lifestyle");
        assertThat(category.getDescription()).isEqualTo("Life articles");
        assertThat(category.getColor()).isEqualTo("#e74c3c");
    }

    @Test
    @DisplayName("Should set and get createdAt")
    void shouldSetAndGetCreatedAt() {
        // Given
        Categories category = new Categories();
        LocalDateTime now = LocalDateTime.now();

        // When
        category.setCreatedAt(now);

        // Then
        assertThat(category.getCreatedAt()).isEqualTo(now);
    }

    @Test
    @DisplayName("Should handle null description and color")
    void shouldHandleNullDescriptionAndColor() {
        // When
        Categories category = Categories.builder()
                .id(1)
                .name("Test")
                .slug("test")
                .description(null)
                .color(null)
                .build();

        // Then
        assertThat(category.getDescription()).isNull();
        assertThat(category.getColor()).isNull();
    }
}

