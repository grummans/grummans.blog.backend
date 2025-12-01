package com.grummans.noyblog.dto;

import com.grummans.noyblog.model.PostAttachments;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;

public class PostDTO {

    @Data
    @NoArgsConstructor
    public static class Req {
        private Integer id;            // Post ID (for update draft)
        private String title;
        private String excerpt;
        private String content;        // HTML content from TipTap Editor (FE sends this)
        private String featuredImageUrl;
        private boolean isFeatured;
        private String status;
        private int categoryId;
        private List<Integer> tagId;
        private String authorUsername;
        private String slug;
        private String metaTitle;
        private String metaDescription;
        private int readingTimeMinutes;
        private LocalDateTime publishedAt;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
    }

    @Data
    @NoArgsConstructor
    @ToString
    public static class Res {
        private int id;
        private String title;
        private String excerpt;
        private int viewCount;
        private String featuredImageUrl;
        private String content;        // Markdown content (NOT returned in API - ignored by mapper)
        private String contentHtml;    // HTML content for display (this is what FE receives)
        private int readingTimeMinutes;
        private String updatedAt;
        private String status;
        private CategoryDTO.CategorySimpleDTO category;
        private List<TagDTO.TagSimpleDTO> tags;
        private String slug;
        private String metaTitle;
        private String metaDescription;
        private UserDTO.AuthorDTO author;
        private List<PostAttachments> attachments; // Separate attachment files (not inline content)
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SimplePostDTO {
        private int id;
        private String title;
        private String slug;
        private String excerpt;
        private String featuredImageUrl;
        private String updatedAt;
    }
}
