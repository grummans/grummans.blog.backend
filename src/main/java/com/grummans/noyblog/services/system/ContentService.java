package com.grummans.noyblog.services.system;

import com.vladsch.flexmark.html2md.converter.FlexmarkHtmlConverter;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ContentService {
    // Note: FlexmarkHtmlConverter is kept for future use if needed
    // Currently we don't convert HTML to Markdown because TipTap Editor works with HTML
    private final FlexmarkHtmlConverter htmlToMdConverter = FlexmarkHtmlConverter.builder().build();

    /**
     * Process content from TipTap Editor (HTML) and prepare for database storage
     * <p>
     * DEPRECATED: This method is no longer used. We now save HTML directly without conversion. TipTap Editor works with
     * HTML, so we keep HTML format for both editing and display.
     *
     * @param htmlContent HTML content from TipTap Editor
     * @return ContentData containing both HTML and Markdown versions
     */
    @Deprecated
    public ContentData processContent(String htmlContent) {
        if (htmlContent == null || htmlContent.trim().isEmpty()) {
            // Return empty strings instead of null to avoid DB null issues
            return new ContentData("", "");
        }

        // Sanitize HTML to prevent XSS attacks
        String sanitizedHtml = sanitizeHtml(htmlContent);

        // Convert HTML → Markdown for editing purposes
        String markdown = htmlToMdConverter.convert(sanitizedHtml);

        return new ContentData(markdown, sanitizedHtml);
    }

    /**
     * Basic HTML sanitization (can be enhanced with libraries like OWASP Java HTML Sanitizer)
     */
    public String sanitizeHtml(String html) {
        if (html == null || html.trim().isEmpty()) {
            return "";
        }

        // Basic sanitization - remove dangerous elements/attributes
        // For production, consider using OWASP Java HTML Sanitizer
        return html.replaceAll("<script[^>]*>.*?</script>", "") // Remove script tags
                .replaceAll("javascript:", "") // Remove javascript: protocols
                .replaceAll("on\\w+\\s*=\\s*[\"'][^\"']*[\"']", ""); // Remove event handlers
    }

    /**
     * Data class to hold both content versions
     */
    @Data
    @AllArgsConstructor
    public static class ContentData {
        private final String markdown;

        private final String html;
    }
}
