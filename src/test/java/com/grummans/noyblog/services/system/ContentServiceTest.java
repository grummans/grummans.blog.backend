package com.grummans.noyblog.services.system;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
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
            assertThat(result).isEqualTo("<p>Hello</p><p>World</p>");
            assertThat(result).doesNotContain("<script>");
            assertThat(result).doesNotContain("alert");
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

        @Test
        @DisplayName("Should remove inline event handlers")
        void shouldRemoveInlineEventHandlers() {
            // Given
            String html = "<img src=\"image.jpg\" onerror=\"alert('XSS')\" />";

            // When
            String result = contentService.sanitizeHtml(html);

            // Then
            assertThat(result).doesNotContain("onerror=");
            assertThat(result).doesNotContain("alert");
        }

        @Test
        @DisplayName("Should remove onclick event handler")
        void shouldRemoveOnclickEventHandler() {
            // Given
            String html = "<button onclick=\"doSomething()\">Click</button>";

            // When
            String result = contentService.sanitizeHtml(html);

            // Then
            assertThat(result).doesNotContain("onclick=");
        }

        @Test
        @DisplayName("Should remove onmouseover event handler")
        void shouldRemoveOnmouseoverEventHandler() {
            // Given
            String html = "<div onmouseover=\"malicious()\">Hover me</div>";

            // When
            String result = contentService.sanitizeHtml(html);

            // Then
            assertThat(result).doesNotContain("onmouseover=");
        }

        @Test
        @DisplayName("Should preserve safe HTML content")
        void shouldPreserveSafeHtmlContent() {
            // Given
            String html = "<h1>Title</h1><p>This is a <strong>bold</strong> paragraph.</p><ul><li>Item 1</li><li>Item 2</li></ul>";

            // When
            String result = contentService.sanitizeHtml(html);

            // Then
            assertThat(result).isEqualTo(html);
        }

        @Test
        @DisplayName("Should preserve images with safe attributes")
        void shouldPreserveImagesWithSafeAttributes() {
            // Given
            String html = "<img src=\"https://example.com/image.jpg\" alt=\"Description\" />";

            // When
            String result = contentService.sanitizeHtml(html);

            // Then
            assertThat(result).isEqualTo(html);
        }

        @Test
        @DisplayName("Should preserve links with safe href")
        void shouldPreserveLinksWithSafeHref() {
            // Given
            String html = "<a href=\"https://example.com\">Link</a>";

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
            assertThat(result).doesNotContain("<script>");
            assertThat(result).doesNotContain("javascript:");
            assertThat(result).doesNotContain("onclick=");
            assertThat(result).doesNotContain("onerror=");
        }
    }
}

