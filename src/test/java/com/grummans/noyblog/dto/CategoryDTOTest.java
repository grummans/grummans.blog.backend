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
            assertThat(req.getName()).isEqualTo("Technology");
            assertThat(req.getSlug()).isEqualTo("technology");
            assertThat(req.getDescription()).isEqualTo("Tech articles");
            assertThat(req.getColor()).isEqualTo("#3498db");
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
            assertThat(req.getName()).isEqualTo("Lifestyle");
            assertThat(req.getSlug()).isEqualTo("lifestyle");
            assertThat(req.getDescription()).isEqualTo("Life articles");
            assertThat(req.getColor()).isEqualTo("#e74c3c");
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
            assertThat(res.getName()).isEqualTo("Technology");
            assertThat(res.getSlug()).isEqualTo("technology");
            assertThat(res.getDescription()).isEqualTo("Tech articles");
            assertThat(res.getColor()).isEqualTo("#3498db");
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
            assertThat(dto.getId()).isEqualTo(1);
            assertThat(dto.getName()).isEqualTo("Technology");
            assertThat(dto.getSlug()).isEqualTo("technology");
            assertThat(dto.getPostCount()).isEqualTo(15);
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
            assertThat(dto.getId()).isEqualTo(2);
            assertThat(dto.getName()).isEqualTo("Lifestyle");
            assertThat(dto.getSlug()).isEqualTo("lifestyle");
            assertThat(dto.getPostCount()).isEqualTo(8);
        }

        @Test
        @DisplayName("Should have working equals and hashCode")
        void shouldHaveWorkingEqualsAndHashCode() {
            // Given
            CategoryDTO.CategorySimpleDTO dto1 = new CategoryDTO.CategorySimpleDTO(1, "Tech", "tech", 10);
            CategoryDTO.CategorySimpleDTO dto2 = new CategoryDTO.CategorySimpleDTO(1, "Tech", "tech", 10);
            CategoryDTO.CategorySimpleDTO dto3 = new CategoryDTO.CategorySimpleDTO(2, "Life", "life", 5);

            // Then
            assertThat(dto1).isEqualTo(dto2);
            assertThat(dto1).isNotEqualTo(dto3);
            assertThat(dto1.hashCode()).isEqualTo(dto2.hashCode());
        }
    }
}

