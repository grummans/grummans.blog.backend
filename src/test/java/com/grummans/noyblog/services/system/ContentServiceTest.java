package com.grummans.noyblog.services.system;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("ContentService Tests")
class ContentServiceTest {

    private ContentService contentService;

    @BeforeEach
    void setUp() {
        contentService = new ContentService();
    }

    @Nested
    @DisplayName("sanitizeHtml")
    class SanitizeHtmlTests {

        @Test
        @DisplayName("Should return empty string for null input")
        void shouldReturnEmptyStringForNullInput() {
            // When
            String result = contentService.sanitizeHtml(null);

            // Then
            assertThat(result).isEmpty();
        }

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = {"   ", "\t", "\n"})
        @DisplayName("Should return empty string for blank input")
        void shouldReturnEmptyStringForBlankInput(String input) {
            // When
            String result = contentService.sanitizeHtml(input);

            // Then
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("Should preserve script tags for Markdown compatibility")
        void shouldPreserveScriptTags() {
            // Given
            String markdown = "```javascript\nconsole.log('Hello');\n```\n\n<script>alert('test')</script>";

            // When
            String result = contentService.sanitizeHtml(markdown);

            // Then
            assertThat(result).isEqualTo(markdown);
        }

        @Test
        @DisplayName("Should preserve javascript protocol in Markdown")
        void shouldPreserveJavascriptProtocol() {
            // Given
            String markdown = "[Click me](javascript:void(0))";

            // When
            String result = contentService.sanitizeHtml(markdown);

            // Then
            assertThat(result).isEqualTo(markdown);
        }

        @ParameterizedTest
        @CsvSource({
                "'![image](image.jpg)', 'Markdown image'",
                "'[link](https://example.com)', 'Markdown link'",
                "'# Heading\n\nParagraph with **bold** text', 'Markdown formatting'",
                "'```java\npublic class Test {}\n```', 'Code block'"
        })
        @DisplayName("Should preserve Markdown content")
        void shouldPreserveMarkdownContent(String markdown, String description) {
            // When
            String result = contentService.sanitizeHtml(markdown);

            // Then
            assertThat(result).isEqualTo(markdown);
        }

        @Test
        @DisplayName("Should preserve HTML tags in Markdown")
        void shouldPreserveHtmlTagsInMarkdown() {
            // Given
            String content = "<div onclick=\"alert(1)\">" +
                    "<script>test()</script>" +
                    "<a href=\"javascript:void(0)\">link</a>" +
                    "</div>";

            // When
            String result = contentService.sanitizeHtml(content);

            // Then
            assertThat(result).isEqualTo(content);
        }
    }
}

