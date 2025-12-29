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

        // Basic sanitization - remove dangerous elements/attributes
        // For production, consider using OWASP Java HTML Sanitizer
        return html.replaceAll("<script[^>]*>.*?</script>", "") // Remove script tags
                .replace("javascript:", "") // Remove javascript: protocols
                .replaceAll("on\\w+\\s*=\\s*[\"'][^\"']*[\"']", ""); // Remove event handlers
    }
}
