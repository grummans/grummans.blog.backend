package com.grummans.noyblog.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Posts Model Tests")
class PostsTest {

    @Test
    @DisplayName("Should create Posts using builder")
    void shouldCreatePostsUsingBuilder() {
        // Given
        LocalDateTime now = LocalDateTime.now();

        // When
        Posts post = Posts.builder()
                .id(1)
                .title("Test Post")
                .slug("test-post")
                .content("Test content")
                .contentHtml("<p>Test content</p>")
                .excerpt("Test excerpt")
                .featuredImageUrl("https://example.com/image.jpg")
                .authorId(1)
                .categoryId(1)
                .status("PUBLISHED")
                .isFeatured(true)
                .viewCount(100)
                .readingTimeMinutes(5)
                .metaTitle("Meta Title")
                .metaDescription("Meta Description")
                .publishedAt(now)
                .build();

        // Then
        assertThat(post.getId()).isEqualTo(1);
        assertThat(post.getTitle()).isEqualTo("Test Post");
        assertThat(post.getSlug()).isEqualTo("test-post");
        assertThat(post.getContent()).isEqualTo("Test content");
        assertThat(post.getContentHtml()).isEqualTo("<p>Test content</p>");
        assertThat(post.getExcerpt()).isEqualTo("Test excerpt");
        assertThat(post.getFeaturedImageUrl()).isEqualTo("https://example.com/image.jpg");
        assertThat(post.getAuthorId()).isEqualTo(1);
        assertThat(post.getCategoryId()).isEqualTo(1);
        assertThat(post.getStatus()).isEqualTo("PUBLISHED");
        assertThat(post.isFeatured()).isTrue();
        assertThat(post.getViewCount()).isEqualTo(100);
        assertThat(post.getReadingTimeMinutes()).isEqualTo(5);
        assertThat(post.getMetaTitle()).isEqualTo("Meta Title");
        assertThat(post.getMetaDescription()).isEqualTo("Meta Description");
        assertThat(post.getPublishedAt()).isEqualTo(now);
    }

    @Test
    @DisplayName("Should create Posts using no-args constructor")
    void shouldCreatePostsUsingNoArgsConstructor() {
        // When
        Posts post = new Posts();
        post.setId(2);
        post.setTitle("Another Post");
        post.setSlug("another-post");
        post.setStatus("DRAFT");
        post.setFeatured(false);

        // Then
        assertThat(post.getId()).isEqualTo(2);
        assertThat(post.getTitle()).isEqualTo("Another Post");
        assertThat(post.getSlug()).isEqualTo("another-post");
        assertThat(post.getStatus()).isEqualTo("DRAFT");
        assertThat(post.isFeatured()).isFalse();
    }

    @Test
    @DisplayName("Should handle DRAFT status")
    void shouldHandleDraftStatus() {
        // When
        Posts post = Posts.builder()
                .id(1)
                .title("Draft Post")
                .status("DRAFT")
                .publishedAt(null)
                .build();

        // Then
        assertThat(post.getStatus()).isEqualTo("DRAFT");
        assertThat(post.getPublishedAt()).isNull();
    }

    @Test
    @DisplayName("Should handle null categoryId for drafts")
    void shouldHandleNullCategoryIdForDrafts() {
        // When
        Posts post = Posts.builder()
                .id(1)
                .title("Draft Post")
                .status("DRAFT")
                .categoryId(null)
                .build();

        // Then
        assertThat(post.getCategoryId()).isNull();
    }

    @Test
    @DisplayName("Should set and get timestamps")
    void shouldSetAndGetTimestamps() {
        // Given
        Posts post = new Posts();
        LocalDateTime now = LocalDateTime.now();

        // When
        post.setCreatedAt(now);
        post.setUpdatedAt(now);
        post.setPublishedAt(now);

        // Then
        assertThat(post.getCreatedAt()).isEqualTo(now);
        assertThat(post.getUpdatedAt()).isEqualTo(now);
        assertThat(post.getPublishedAt()).isEqualTo(now);
    }

    @Test
    @DisplayName("Should initialize postTags as empty set")
    void shouldInitializePostTagsAsEmptySet() {
        // When
        Posts post = new Posts();

        // Then
        assertThat(post.getPostTags()).isNotNull();
        assertThat(post.getPostTags()).isEmpty();
    }
}

