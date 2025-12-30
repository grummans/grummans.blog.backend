package com.grummans.noyblog.repository;

import com.grummans.noyblog.model.PostTags;
import com.grummans.noyblog.model.Posts;
import com.grummans.noyblog.model.Tags;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface PostTagsRepository extends JpaRepository<PostTags, Integer> {
    @Query("SELECT pt.tag FROM PostTags pt WHERE pt.post.id = :postId")
    List<Tags> findByPostId(@Param("postId") Integer postId);

    @Query("SELECT pt.post FROM PostTags pt WHERE pt.tag.id = :tagId")
    Posts findByTagId(@Param("tagId") Integer tagId);

    @Transactional
    @Modifying
    @Query("DELETE FROM PostTags pt WHERE pt.post.id = :postId")
    void deleteAllByPostId(@Param("postId") int postId);

    int countByTagId(@Param("postId") int tagId);
}
