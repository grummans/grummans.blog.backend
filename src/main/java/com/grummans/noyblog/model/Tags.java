package com.grummans.noyblog.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table (name = "tags")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EntityListeners (AuditingEntityListener.class)
public class Tags {

    @Id
    private int id;

    @Column (name = "name", nullable = false, unique = true)
    private String name;

    @Column (name = "slug", nullable = false, unique = true)
    private String slug;

    @CreatedDate
    @Column (name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @OneToMany (mappedBy = "tag")
    private Set<PostTags> postTags = new HashSet<>();

}
