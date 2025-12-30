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
        @DisplayName("Should remove script tags")
        void shouldRemoveScriptTags() {
            // Given
            String html = "<p>Hello</p><script>alert('XSS')</script><p>World</p>";

            // When
            String result = contentService.sanitizeHtml(html);

            // Then
            assertThat(result)
                    .isEqualTo("<p>Hello</p><p>World</p>")
                    .doesNotContain("<script>")
                    .doesNotContain("alert");
        }

        @Test
        @DisplayName("Should remove script tags with attributes")
        void shouldRemoveScriptTagsWithAttributes() {
            // Given
            String html = "<div><script type=\"text/javascript\">malicious code</script></div>";

            // When
            String result = contentService.sanitizeHtml(html);

            // Then
            assertThat(result).isEqualTo("<div></div>");
        }

        @Test
        @DisplayName("Should remove javascript protocol")
        void shouldRemoveJavascriptProtocol() {
            // Given
            String html = "<a href=\"javascript:alert('XSS')\">Click me</a>";

            // When
            String result = contentService.sanitizeHtml(html);

            // Then
            assertThat(result).doesNotContain("javascript:");
        }

        @ParameterizedTest
        @CsvSource({
                "'<img src=\"image.jpg\" onerror=\"alert()\" />', 'onerror='",
                "'<button onclick=\"doSomething()\">Click</button>', 'onclick='",
                "'<div onmouseover=\"malicious()\">Hover me</div>', 'onmouseover='"
        })
        @DisplayName("Should remove inline event handlers")
        void shouldRemoveInlineEventHandlers(String html, String forbidden) {
            // When
            String result = contentService.sanitizeHtml(html);

            // Then
            assertThat(result).doesNotContain(forbidden);
        }

        @ParameterizedTest
        @ValueSource(strings = {
                "<h1>Title</h1><p>This is a <strong>bold</strong> paragraph.</p><ul><li>Item 1</li><li>Item 2</li></ul>",
                "<img src=\"https://example.com/image.jpg\" alt=\"Description\" />",
                "<a href=\"https://example.com\">Link</a>"
        })
        @DisplayName("Should preserve safe HTML content")
        void shouldPreserveSafeHtmlContent(String html) {
            // When
            String result = contentService.sanitizeHtml(html);

            // Then
            assertThat(result).isEqualTo(html);
        }

        @Test
        @DisplayName("Should handle multiple XSS attempts")
        void shouldHandleMultipleXssAttempts() {
            // Given
            String html = "<div onclick=\"alert(1)\">" +
                    "<script>evil()</script>" +
                    "<a href=\"javascript:void(0)\">link</a>" +
                    "<img src=\"x\" onerror=\"hack()\">" +
                    "</div>";

            // When
            String result = contentService.sanitizeHtml(html);

            // Then
            assertThat(result)
                    .doesNotContain("<script>")
                    .doesNotContain("javascript:")
                    .doesNotContain("onclick=")
                    .doesNotContain("onerror=");
        }
    }
}

