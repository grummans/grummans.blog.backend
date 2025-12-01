package com.grummans.noyblog.model;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table (name = "posts")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EntityListeners (AuditingEntityListener.class)
public class Posts {

    @Id
    @Column (name = "id", nullable = false)
    @GeneratedValue (strategy = jakarta.persistence.GenerationType.IDENTITY)
    private int id;

    @Column (name = "title")
    private String title;

    @Column (name = "slug")
    private String slug;

    @Column (name = "content", columnDefinition = "TEXT")
    private String content;

    @Column (name = "content_html", columnDefinition = "TEXT")
    private String contentHtml;

    @Column (name = "excerpt")
    private String excerpt;

    @Column (name = "featured_image_url")
    private String featuredImageUrl;

    @Column (name = "author_id")
    private int authorId;

    @Column (name = "category_id")
    private Integer categoryId;

    @Column (name = "status")
    private String status;

    @Column (name = "is_featured",columnDefinition = "BOOLEAN DEFAULT FALSE")
    private boolean isFeatured;

    @Column (name = "view_count")
    private int viewCount;

    @Column (name = "reading_time_minutes")
    private int readingTimeMinutes;

    @Column (name = "meta_title")
    private String metaTitle;

    @Column (name = "meta_description")
    private String metaDescription;

    @Column (name = "published_at")
    private LocalDateTime publishedAt;

    @Column (name = "created_at")
    @CreatedDate
    private LocalDateTime createdAt;

    @Column (name = "updated_at")
    @LastModifiedDate
    private LocalDateTime updatedAt;

    @OneToMany (mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<PostTags> postTags = new HashSet<>();

}
