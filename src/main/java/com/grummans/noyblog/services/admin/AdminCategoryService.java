package com.grummans.noyblog.services.admin;

import com.grummans.noyblog.dto.CategoryDTO;
import com.grummans.noyblog.mapper.CategoryMapper;
import com.grummans.noyblog.model.Categories;
import com.grummans.noyblog.repository.CategoryRepository;
import com.grummans.noyblog.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminCategoryService {

    private final CategoryRepository categoryRepository;
    private final PostRepository postRepository;
    private final CategoryMapper categoryMapper;

    public List<CategoryDTO.CategorySimpleDTO> getAllCategories() {
        List<Categories> categories = categoryRepository.findAll();

        return categories.stream().map(category -> {
            CategoryDTO.CategorySimpleDTO dto = categoryMapper.toCategorySimpleDTO(category);
            int postCount = postRepository.countByCategoryId(category.getId());
            dto.setPostCount(postCount);
            return dto;
        }).toList();
    }

    public CategoryDTO.CategorySimpleDTO createCategory(CategoryDTO.Req req) {
        Categories category = categoryMapper.toCategory(req);
        Categories savedCategory = categoryRepository.save(category);
        return categoryMapper.toCategorySimpleDTO(savedCategory);
    }

    public CategoryDTO.Res getDetailCategory(int categoryId) {
        Categories category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new RuntimeException("Category not found with id: " + categoryId));
        return categoryMapper.toCategoryRes(category);
    }

    public CategoryDTO.CategorySimpleDTO updateCategory(int categoryId, CategoryDTO.Req req) {
        Categories category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new RuntimeException("Category not found with id: " + categoryId));
        category.setName(req.getName());
        category.setSlug(req.getSlug());
        category.setDescription(req.getDescription());
        category.setColor(req.getColor());
        Categories updatedCategory = categoryRepository.save(category);
        return categoryMapper.toCategorySimpleDTO(updatedCategory);
    }

    public void deleteCategory(int categoryId) {
        Categories category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new RuntimeException("Category not found with id: " + categoryId));
        categoryRepository.delete(category);
    }
}
