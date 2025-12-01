package com.grummans.noyblog.repository;

import com.grummans.noyblog.model.PostAttachments;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PostAttachmentRepository extends JpaRepository<PostAttachments, Integer> {

    List<PostAttachments> findByPostId(Integer postId);
}
