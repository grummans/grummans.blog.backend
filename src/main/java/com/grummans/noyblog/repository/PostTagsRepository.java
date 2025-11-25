package com.grummans.noyblog.repository;

import com.grummans.noyblog.model.PostTags;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;

@Repository
public interface PostTagsRepository extends JpaRepository<PostTags, Integer> {

}
