package com.grummans.noyblog.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.grummans.noyblog.model.PostAttachments;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

public class PostDTO {

    private PostDTO() {
        // Utility class - hide implicit public constructor
    }

    @Data
    @NoArgsConstructor
    public static class Req {
        private Integer id;            // Post ID (for update draft)
        private String title;
        private String excerpt;
        private String content;        // HTML content from TipTap Editor (FE sends this)
        private String featuredImageUrl;
        @JsonProperty("isFeatured")
        private boolean isFeatured;    // Need @JsonProperty due to Lombok boolean naming convention
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
        @JsonProperty("isFeatured")
        private boolean isFeatured;
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

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PostForClientDTO {
        private int id;
        private String title;
        private String slug;
        private String excerpt;
        private String featuredImageUrl;
        private CategoryDTO.CategorySimpleDTO category;
        private List<TagDTO.TagSimpleDTO> tags;
        private int readingTimeMinutes;
        private String updatedAt;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DashboardPostDTO {
        private int id;
        private String title;
        private String slug;
        private String updatedAt;
    }
}
