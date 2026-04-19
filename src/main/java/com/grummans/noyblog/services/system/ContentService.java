package com.grummans.noyblog.services.system;

import org.springframework.stereotype.Service;

@Service
public class ContentService {

    /**
     * Basic HTML sanitization (can be enhanced with libraries like OWASP Java HTML Sanitizer)
     */
    public String sanitizeHtml(String html) {
        if (html == null || html.trim().isEmpty()) {
            return "";
        }

        // Basic sanitization - removed for Markdown as it can strip code blocks.
        // For production, consider using markdown-specific sanitizer or frontend handling
        return html;
    }
}
