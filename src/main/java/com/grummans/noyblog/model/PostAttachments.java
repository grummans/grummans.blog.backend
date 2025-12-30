package com.grummans.noyblog.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "post_attachments")
@Data
@EntityListeners(AuditingEntityListener.class)
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PostAttachments {

    @Id
    @Column(name = "id", nullable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(name = "post_id", nullable = false)
    private int postId;

    @Column(name = "original_filename", nullable = false)
    private String originalFilename;

    @Column(name = "stored_filename", nullable = false)
    private String storedFilename;

    @Column(name = "file_type", nullable = false)
    private String fileType;

    @Column(name = "mime_type", nullable = false)
    private String mimeType;

    @Column(name = "file_size", nullable = false)
    private long fileSize;

    @Column(name = "storage_path", nullable = false)
    private String storagePath;

    @Column(name = "alt_text")
    private String altText;

    @Column(name = "display_order")
    private int displayOrder;

    @Column(name = "uploaded_at", nullable = false)
    @LastModifiedDate
    private LocalDateTime uploadedAt;
}
