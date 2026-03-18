package com.grummans.noyblog.services.client;

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
public class ClientCategoryService {

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
}
