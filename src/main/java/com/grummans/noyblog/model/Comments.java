package com.grummans.noyblog.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "comments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Comments {

    @Id
    @Column(name = "id", nullable = false)
    @NonNull
    @GeneratedValue(strategy = jakarta.persistence.GenerationType.IDENTITY)
    private String id;

    @Column(name = "post_id", nullable = false)
    private String postId;

    @Column(name = "parent_comment_id")
    private String parentCommentId;

    @Column(name = "author_name", nullable = false)
    private String authorName;

    @Column(name = "author_email", nullable = false)
    private String authorEmail;

    @Column(name = "is_approved", nullable = false, columnDefinition = "boolean default false")
    private boolean isApproved;

    @Column(name = "content", nullable = false)
    private String content;

    @Column(name = "created_at", nullable = false, updatable = false)
    private String createdAt;
}
