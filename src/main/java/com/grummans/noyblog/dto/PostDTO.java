package com.grummans.noyblog.dto;

import com.grummans.noyblog.model.Categories;
import com.grummans.noyblog.model.Tags;
import com.grummans.noyblog.model.Users;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;

public class PostDTO {

    @Data
    @NoArgsConstructor
    public static class Req {
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
        private String content;        // Markdown content (optional)
        private String contentHtml;    // HTML content for display
        private int readingTimeMinutes;
        private String updatedAt;
        private String status;
        private CategoryDTO.CategorySimpleDTO category;
        private List<TagDTO.TagSimpleDTO> tags;
        private String slug;
        private String metaTitle;
        private String metaDescription;
        private UserDTO.AuthorDTO author;
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
