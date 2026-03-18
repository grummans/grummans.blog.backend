package com.grummans.noyblog.services.client;

import com.grummans.noyblog.dto.CategoryDTO;
import com.grummans.noyblog.mapper.CategoryMapper;
import com.grummans.noyblog.model.Categories;
import com.grummans.noyblog.repository.CategoryRepository;
import com.grummans.noyblog.repository.PostRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ClientCategoryService Tests")
class ClientCategoryServiceTest {

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private PostRepository postRepository;

    @Mock
    private CategoryMapper categoryMapper;

    @InjectMocks
    private ClientCategoryService clientCategoryService;

    private Categories testCategory1;
    private Categories testCategory2;

    @BeforeEach
    void setUp() {
        testCategory1 = Categories.builder()
                .id(1)
                .name("Technology")
                .slug("technology")
                .description("Tech articles")
                .color("#3498db")
                .build();

        testCategory2 = Categories.builder()
                .id(2)
                .name("Lifestyle")
                .slug("lifestyle")
                .description("Life articles")
                .color("#e74c3c")
                .build();
    }

    @Test
    @DisplayName("Should return all categories with post count")
    void shouldReturnAllCategoriesWithPostCount() {
        // Given
        List<Categories> categories = Arrays.asList(testCategory1, testCategory2);

        CategoryDTO.CategorySimpleDTO dto1 = new CategoryDTO.CategorySimpleDTO(1, "Technology", "technology", 0);
        CategoryDTO.CategorySimpleDTO dto2 = new CategoryDTO.CategorySimpleDTO(2, "Lifestyle", "lifestyle", 0);

        when(categoryRepository.findAll()).thenReturn(categories);
        when(categoryMapper.toCategorySimpleDTO(testCategory1)).thenReturn(dto1);
        when(categoryMapper.toCategorySimpleDTO(testCategory2)).thenReturn(dto2);
        when(postRepository.countByCategoryId(1)).thenReturn(15);
        when(postRepository.countByCategoryId(2)).thenReturn(8);

        // When
        List<CategoryDTO.CategorySimpleDTO> result = clientCategoryService.getAllCategories();

        // Then
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getPostCount()).isEqualTo(15);
        assertThat(result.get(1).getPostCount()).isEqualTo(8);
        verify(categoryRepository).findAll();
        verify(postRepository, times(2)).countByCategoryId(anyInt());
    }

    @Test
    @DisplayName("Should return empty list when no categories exist")
    void shouldReturnEmptyListWhenNoCategoriesExist() {
        // Given
        when(categoryRepository.findAll()).thenReturn(List.of());

        // When
        List<CategoryDTO.CategorySimpleDTO> result = clientCategoryService.getAllCategories();

        // Then
        assertThat(result).isEmpty();
        verify(categoryRepository).findAll();
        verify(postRepository, never()).countByCategoryId(anyInt());
    }
}

