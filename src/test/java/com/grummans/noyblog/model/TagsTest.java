package com.grummans.noyblog.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Tags Model Tests")
class TagsTest {

    @Test
    @DisplayName("Should create Tags using builder")
    void shouldCreateTagsUsingBuilder() {
        // When
        Tags tag = Tags.builder()
                .id(1)
                .name("Java")
                .slug("java")
                .build();

        // Then
        assertThat(tag.getId()).isEqualTo(1);
        assertThat(tag.getName()).isEqualTo("Java");
        assertThat(tag.getSlug()).isEqualTo("java");
    }

    @Test
    @DisplayName("Should create Tags using no-args constructor")
    void shouldCreateTagsUsingNoArgsConstructor() {
        // When
        Tags tag = new Tags();
        tag.setId(2);
        tag.setName("Spring");
        tag.setSlug("spring");

        // Then
        assertThat(tag.getId()).isEqualTo(2);
        assertThat(tag.getName()).isEqualTo("Spring");
        assertThat(tag.getSlug()).isEqualTo("spring");
    }

    @Test
    @DisplayName("Should set and get createdAt")
    void shouldSetAndGetCreatedAt() {
        // Given
        Tags tag = new Tags();
        LocalDateTime now = LocalDateTime.now();

        // When
        tag.setCreatedAt(now);

        // Then
        assertThat(tag.getCreatedAt()).isEqualTo(now);
    }
}

