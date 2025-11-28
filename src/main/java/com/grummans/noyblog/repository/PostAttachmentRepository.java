package com.grummans.noyblog.repository;


import com.grummans.noyblog.model.PostAttachment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PostAttachmentRepository extends JpaRepository<PostAttachment, Integer> {
}
