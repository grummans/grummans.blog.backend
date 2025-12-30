package com.grummans.noyblog.dto;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("CategoryDTO Tests")
class CategoryDTOTest {

    @Nested
    @DisplayName("CategoryDTO.Req Tests")
    class ReqTests {

        @Test
        @DisplayName("Should create Req with all fields")
        void shouldCreateReqWithAllFields() {
            // When
            CategoryDTO.Req req = new CategoryDTO.Req("Technology", "technology", "Tech articles", "#3498db");

            // Then
            assertThat(req)
                    .extracting(CategoryDTO.Req::getName, CategoryDTO.Req::getSlug, CategoryDTO.Req::getDescription, CategoryDTO.Req::getColor)
                    .containsExactly("Technology", "technology", "Tech articles", "#3498db");
        }

        @Test
        @DisplayName("Should create Req with no-args constructor")
        void shouldCreateReqWithNoArgsConstructor() {
            // When
            CategoryDTO.Req req = new CategoryDTO.Req();
            req.setName("Lifestyle");
            req.setSlug("lifestyle");
            req.setDescription("Life articles");
            req.setColor("#e74c3c");

            // Then
            assertThat(req)
                    .extracting(CategoryDTO.Req::getName, CategoryDTO.Req::getSlug, CategoryDTO.Req::getDescription, CategoryDTO.Req::getColor)
                    .containsExactly("Lifestyle", "lifestyle", "Life articles", "#e74c3c");
        }
    }

    @Nested
    @DisplayName("CategoryDTO.Res Tests")
    class ResTests {

        @Test
        @DisplayName("Should create Res with all fields")
        void shouldCreateResWithAllFields() {
            // When
            CategoryDTO.Res res = new CategoryDTO.Res("Technology", "technology", "Tech articles", "#3498db");

            // Then
            assertThat(res)
                    .extracting(CategoryDTO.Res::getName, CategoryDTO.Res::getSlug, CategoryDTO.Res::getDescription, CategoryDTO.Res::getColor)
                    .containsExactly("Technology", "technology", "Tech articles", "#3498db");
        }
    }

    @Nested
    @DisplayName("CategoryDTO.CategorySimpleDTO Tests")
    class CategorySimpleDTOTests {

        @Test
        @DisplayName("Should create CategorySimpleDTO with all fields")
        void shouldCreateCategorySimpleDTOWithAllFields() {
            // When
            CategoryDTO.CategorySimpleDTO dto = new CategoryDTO.CategorySimpleDTO(1, "Technology", "technology", 15);

            // Then
            assertThat(dto)
                    .extracting(CategoryDTO.CategorySimpleDTO::getId, CategoryDTO.CategorySimpleDTO::getName, CategoryDTO.CategorySimpleDTO::getSlug, CategoryDTO.CategorySimpleDTO::getPostCount)
                    .containsExactly(1, "Technology", "technology", 15);
        }

        @Test
        @DisplayName("Should create CategorySimpleDTO with no-args constructor")
        void shouldCreateCategorySimpleDTOWithNoArgsConstructor() {
            // When
            CategoryDTO.CategorySimpleDTO dto = new CategoryDTO.CategorySimpleDTO();
            dto.setId(2);
            dto.setName("Lifestyle");
            dto.setSlug("lifestyle");
            dto.setPostCount(8);

            // Then
            assertThat(dto)
                    .extracting(CategoryDTO.CategorySimpleDTO::getId, CategoryDTO.CategorySimpleDTO::getName, CategoryDTO.CategorySimpleDTO::getSlug, CategoryDTO.CategorySimpleDTO::getPostCount)
                    .containsExactly(2, "Lifestyle", "lifestyle", 8);
        }

        @Test
        @DisplayName("Should have working equals and hashCode")
        void shouldHaveWorkingEqualsAndHashCode() {
            // Given
            CategoryDTO.CategorySimpleDTO dto1 = new CategoryDTO.CategorySimpleDTO(1, "Tech", "tech", 10);
            CategoryDTO.CategorySimpleDTO dto2 = new CategoryDTO.CategorySimpleDTO(1, "Tech", "tech", 10);
            CategoryDTO.CategorySimpleDTO dto3 = new CategoryDTO.CategorySimpleDTO(2, "Life", "life", 5);

            // Then
            assertThat(dto1)
                    .isEqualTo(dto2)
                    .isNotEqualTo(dto3)
                    .hasSameHashCodeAs(dto2);
        }
    }
}

