package com.grummans.noyblog.services.admin;

import com.grummans.noyblog.dto.CategoryDTO;
import com.grummans.noyblog.mapper.CategoryMapper;
import com.grummans.noyblog.model.Categories;
import com.grummans.noyblog.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;

    public List<CategoryDTO.CategorySimpleDTO> getAllCategories() {
        List<Categories> categories = categoryRepository.findAll();
        return categories.stream()
                .map(categoryMapper::toCategorySimpleDTO)
                .toList();
    }

    public CategoryDTO.CategorySimpleDTO createCategory(CategoryDTO.Req req) {
        Categories category = categoryMapper.toCategory(req);
        Categories savedCategory = categoryRepository.save(category);
        return categoryMapper.toCategorySimpleDTO(savedCategory);
    }

    public CategoryDTO.CategorySimpleDTO updateCategory(CategoryDTO.Req req) {
        Categories category = categoryMapper.toCategory(req);
        Categories updatedCategory = categoryRepository.save(category);
        return categoryMapper.toCategorySimpleDTO(updatedCategory);
    }

    public void deleteCategory(int categoryId) {
        Categories category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new RuntimeException("Category not found with id: " + categoryId));
        categoryRepository.delete(category);
    }
}
