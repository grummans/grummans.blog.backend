package com.grummans.noyblog.repository;

import com.grummans.noyblog.dto.PostDTO;
import com.grummans.noyblog.model.Posts;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PostRepository extends JpaRepository<Posts, Integer> {

    Page<Posts> findByTitleContainingIgnoreCase(String title, Pageable pageable);
}
