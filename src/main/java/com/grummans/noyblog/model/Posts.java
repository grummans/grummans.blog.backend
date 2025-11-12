package com.grummans.noyblog.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table (name = "posts")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Posts {

    @Id
    private int id;
    private String title;
    private String slug;
    private String content;
    private String contentHtml;
    private String excerpt;
    private String featureImageUrl;
    private int authorId;
    private int categoryId;
    private String status;
    private boolean isFeatured;
    private int viewCount;
    private int readTimeMinutes;
    private String metaTitle;
    private String metaDescription;
    private String publishedAt;
    private String createdAt;
    private String updatedAt;
}
