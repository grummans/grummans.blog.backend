package com.grummans.noyblog.dto;

import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;

public class PostDTO {

    @Data
    @NoArgsConstructor
    public static class Req {
        private String title;
        private String excerpt;
        private String content;
        private String featuredImageUrl;
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
        private int readingTimeMinutes;
        private String updatedAt;
        private String status;
        private int categoryId;
        private List<Integer> tagId;
        private String slug;
        private String metaTitle;
        private String metaDescription;
        private String authorId;
    }
}
