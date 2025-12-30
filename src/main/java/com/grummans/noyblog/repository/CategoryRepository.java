package com.grummans.noyblog.repository;

import com.grummans.noyblog.model.Categories;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CategoryRepository extends JpaRepository<Categories, Integer> {
}
