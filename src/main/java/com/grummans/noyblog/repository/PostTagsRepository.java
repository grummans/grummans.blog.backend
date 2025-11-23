package com.grummans.noyblog.repository;

import com.grummans.noyblog.model.PostTags;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PostTagsRepository extends JpaRepository<PostTags, Integer> {

}
