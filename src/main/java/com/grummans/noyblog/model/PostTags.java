package com.grummans.noyblog.model;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Entity
@Table(name = "post_tags")
@Data
@IdClass(PostTagId.class)
public class PostTags {

    @Id
    @JoinColumn(name = "post_id")
    @ManyToOne(fetch = FetchType.LAZY)
    private Posts post;

    @Id
    @JoinColumn(name = "tag_id")
    @ManyToOne(fetch = FetchType.LAZY)
    private Tags tag;

}

@Data
@NoArgsConstructor
@AllArgsConstructor
class PostTagId implements Serializable {
    private int post;
    private int tag;
}
