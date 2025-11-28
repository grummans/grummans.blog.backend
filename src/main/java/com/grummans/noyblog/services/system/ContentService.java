package com.grummans.noyblog.services.system;

import com.vladsch.flexmark.html2md.converter.FlexmarkHtmlConverter;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ContentService {

    private final FlexmarkHtmlConverter htmlToMdConverter = FlexmarkHtmlConverter.builder().build();

    /**
     * Process content from TipTap Editor (HTML) and prepare for database storage
     *
     * @param htmlContent HTML content from TipTap Editor
     * @return ContentData containing both HTML and Markdown versions
     */
    public ContentData processContent(String htmlContent) {
        if (htmlContent == null || htmlContent.trim().isEmpty()) {
            return new ContentData(null, null);
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
    private String sanitizeHtml(String html) {
        if (html == null) return null;

        // Basic sanitization - remove dangerous elements/attributes
        // For production, consider using OWASP Java HTML Sanitizer
        return html
                .replaceAll("<script[^>]*>.*?</script>", "") // Remove script tags
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
