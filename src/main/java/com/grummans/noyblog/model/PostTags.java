package com.grummans.noyblog.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Setter;

@Entity
@Table (name = "post_tags")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PostTags {

    @Id
    @Column(name = "id", nullable = false)
    @NonNull
    @GeneratedValue(strategy = jakarta.persistence.GenerationType.IDENTITY)
    private int postId;

    @Column(name = "tag_id", nullable = false)
    private int tagId;
}
