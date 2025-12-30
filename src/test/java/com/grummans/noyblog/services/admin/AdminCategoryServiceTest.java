package com.grummans.noyblog.services.admin;

import com.grummans.noyblog.dto.CategoryDTO;
import com.grummans.noyblog.mapper.CategoryMapper;
import com.grummans.noyblog.model.Categories;
import com.grummans.noyblog.repository.CategoryRepository;
import com.grummans.noyblog.repository.PostRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AdminCategoryService Tests")
class AdminCategoryServiceTest {

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private PostRepository postRepository;

    @Mock
    private CategoryMapper categoryMapper;

    @InjectMocks
    private AdminCategoryService adminCategoryService;

    private Categories testCategory;
    private CategoryDTO.CategorySimpleDTO testCategoryDTO;
    private CategoryDTO.Req testCategoryReq;
    private CategoryDTO.Res testCategoryRes;

    @BeforeEach
    void setUp() {
        testCategory = Categories.builder()
                .id(1)
                .name("Technology")
                .slug("technology")
                .description("Tech articles")
                .color("#3498db")
                .build();

        testCategoryDTO = new CategoryDTO.CategorySimpleDTO(1, "Technology", "technology", 10);
        testCategoryReq = new CategoryDTO.Req("Technology", "technology", "Tech articles", "#3498db");
        testCategoryRes = new CategoryDTO.Res("Technology", "technology", "Tech articles", "#3498db");
    }

    @Nested
    @DisplayName("getAllCategories")
    class GetAllCategoriesTests {

        @Test
        @DisplayName("Should return all categories with post count")
        void shouldReturnAllCategoriesWithPostCount() {
            // Given
            Categories cat1 = Categories.builder().id(1).name("Tech").slug("tech").build();
            Categories cat2 = Categories.builder().id(2).name("Life").slug("life").build();
            List<Categories> categories = Arrays.asList(cat1, cat2);

            CategoryDTO.CategorySimpleDTO dto1 = new CategoryDTO.CategorySimpleDTO(1, "Tech", "tech", 0);
            CategoryDTO.CategorySimpleDTO dto2 = new CategoryDTO.CategorySimpleDTO(2, "Life", "life", 0);

            when(categoryRepository.findAll()).thenReturn(categories);
            when(categoryMapper.toCategorySimpleDTO(cat1)).thenReturn(dto1);
            when(categoryMapper.toCategorySimpleDTO(cat2)).thenReturn(dto2);
            when(postRepository.countByCategoryId(1)).thenReturn(5);
            when(postRepository.countByCategoryId(2)).thenReturn(3);

            // When
            List<CategoryDTO.CategorySimpleDTO> result = adminCategoryService.getAllCategories();

            // Then
            assertThat(result).hasSize(2);
            assertThat(result.get(0).getPostCount()).isEqualTo(5);
            assertThat(result.get(1).getPostCount()).isEqualTo(3);
            verify(categoryRepository).findAll();
        }

        @Test
        @DisplayName("Should return empty list when no categories exist")
        void shouldReturnEmptyListWhenNoCategoriesExist() {
            // Given
            when(categoryRepository.findAll()).thenReturn(List.of());

            // When
            List<CategoryDTO.CategorySimpleDTO> result = adminCategoryService.getAllCategories();

            // Then
            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("createCategory")
    class CreateCategoryTests {

        @Test
        @DisplayName("Should create category successfully")
        void shouldCreateCategorySuccessfully() {
            // Given
            when(categoryMapper.toCategory(testCategoryReq)).thenReturn(testCategory);
            when(categoryRepository.save(testCategory)).thenReturn(testCategory);
            when(categoryMapper.toCategorySimpleDTO(testCategory)).thenReturn(testCategoryDTO);

            // When
            CategoryDTO.CategorySimpleDTO result = adminCategoryService.createCategory(testCategoryReq);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getName()).isEqualTo("Technology");
            assertThat(result.getSlug()).isEqualTo("technology");
            verify(categoryRepository).save(any(Categories.class));
        }
    }

    @Nested
    @DisplayName("getDetailCategory")
    class GetDetailCategoryTests {

        @Test
        @DisplayName("Should return category when found")
        void shouldReturnCategoryWhenFound() {
            // Given
            when(categoryRepository.findById(1)).thenReturn(Optional.of(testCategory));
            when(categoryMapper.toCategoryRes(testCategory)).thenReturn(testCategoryRes);

            // When
            CategoryDTO.Res result = adminCategoryService.getDetailCategory(1);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getName()).isEqualTo("Technology");
        }

        @Test
        @DisplayName("Should throw exception when category not found")
        void shouldThrowExceptionWhenCategoryNotFound() {
            // Given
            when(categoryRepository.findById(999)).thenReturn(Optional.empty());

            // When/Then
            assertThatThrownBy(() -> adminCategoryService.getDetailCategory(999))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("Category not found with id: 999");
        }
    }

    @Nested
    @DisplayName("updateCategory")
    class UpdateCategoryTests {

        @Test
        @DisplayName("Should update category successfully")
        void shouldUpdateCategorySuccessfully() {
            // Given
            CategoryDTO.Req updateReq = new CategoryDTO.Req("Updated Tech", "updated-tech", "Updated description", "#e74c3c");
            Categories updatedCategory = Categories.builder()
                    .id(1)
                    .name("Updated Tech")
                    .slug("updated-tech")
                    .description("Updated description")
                    .color("#e74c3c")
                    .build();
            CategoryDTO.CategorySimpleDTO updatedDTO = new CategoryDTO.CategorySimpleDTO(1, "Updated Tech", "updated-tech", 10);

            when(categoryRepository.findById(1)).thenReturn(Optional.of(testCategory));
            when(categoryRepository.save(testCategory)).thenReturn(updatedCategory);
            when(categoryMapper.toCategorySimpleDTO(updatedCategory)).thenReturn(updatedDTO);

            // When
            CategoryDTO.CategorySimpleDTO result = adminCategoryService.updateCategory(1, updateReq);

            // Then
            assertThat(result.getName()).isEqualTo("Updated Tech");
            assertThat(result.getSlug()).isEqualTo("updated-tech");
            verify(categoryRepository).save(testCategory);
        }

        @Test
        @DisplayName("Should throw exception when updating non-existent category")
        void shouldThrowExceptionWhenUpdatingNonExistentCategory() {
            // Given
            when(categoryRepository.findById(999)).thenReturn(Optional.empty());

            // When/Then
            assertThatThrownBy(() -> adminCategoryService.updateCategory(999, testCategoryReq))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("Category not found with id: 999");
        }
    }

    @Nested
    @DisplayName("deleteCategory")
    class DeleteCategoryTests {

        @Test
        @DisplayName("Should delete category successfully")
        void shouldDeleteCategorySuccessfully() {
            // Given
            when(categoryRepository.findById(1)).thenReturn(Optional.of(testCategory));
            doNothing().when(categoryRepository).delete(testCategory);

            // When
            adminCategoryService.deleteCategory(1);

            // Then
            verify(categoryRepository).delete(testCategory);
        }

        @Test
        @DisplayName("Should throw exception when deleting non-existent category")
        void shouldThrowExceptionWhenDeletingNonExistentCategory() {
            // Given
            when(categoryRepository.findById(999)).thenReturn(Optional.empty());

            // When/Then
            assertThatThrownBy(() -> adminCategoryService.deleteCategory(999))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("Category not found with id: 999");
        }
    }
}

