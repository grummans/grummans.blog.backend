package com.grummans.noyblog.dto;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("TagDTO Tests")
class TagDTOTest {

    @Nested
    @DisplayName("TagDTO.Req Tests")
    class ReqTests {

        @Test
        @DisplayName("Should create Req with all fields")
        void shouldCreateReqWithAllFields() {
            // When
            TagDTO.Req req = new TagDTO.Req("Java", "java");

            // Then
            assertThat(req.getName()).isEqualTo("Java");
            assertThat(req.getSlug()).isEqualTo("java");
        }

        @Test
        @DisplayName("Should create Req with no-args constructor")
        void shouldCreateReqWithNoArgsConstructor() {
            // When
            TagDTO.Req req = new TagDTO.Req();
            req.setName("Spring");
            req.setSlug("spring");

            // Then
            assertThat(req.getName()).isEqualTo("Spring");
            assertThat(req.getSlug()).isEqualTo("spring");
        }
    }

    @Nested
    @DisplayName("TagDTO.TagSimpleDTO Tests")
    class TagSimpleDTOTests {

        @Test
        @DisplayName("Should create TagSimpleDTO with all fields")
        void shouldCreateTagSimpleDTOWithAllFields() {
            // When
            TagDTO.TagSimpleDTO dto = new TagDTO.TagSimpleDTO(1, "Java", "java", 10);

            // Then
            assertThat(dto.getId()).isEqualTo(1);
            assertThat(dto.getName()).isEqualTo("Java");
            assertThat(dto.getSlug()).isEqualTo("java");
            assertThat(dto.getPostCount()).isEqualTo(10);
        }

        @Test
        @DisplayName("Should create TagSimpleDTO with no-args constructor")
        void shouldCreateTagSimpleDTOWithNoArgsConstructor() {
            // When
            TagDTO.TagSimpleDTO dto = new TagDTO.TagSimpleDTO();
            dto.setId(2);
            dto.setName("Spring");
            dto.setSlug("spring");
            dto.setPostCount(5);

            // Then
            assertThat(dto.getId()).isEqualTo(2);
            assertThat(dto.getName()).isEqualTo("Spring");
            assertThat(dto.getSlug()).isEqualTo("spring");
            assertThat(dto.getPostCount()).isEqualTo(5);
        }

        @Test
        @DisplayName("Should have working equals and hashCode")
        void shouldHaveWorkingEqualsAndHashCode() {
            // Given
            TagDTO.TagSimpleDTO dto1 = new TagDTO.TagSimpleDTO(1, "Java", "java", 10);
            TagDTO.TagSimpleDTO dto2 = new TagDTO.TagSimpleDTO(1, "Java", "java", 10);
            TagDTO.TagSimpleDTO dto3 = new TagDTO.TagSimpleDTO(2, "Spring", "spring", 5);

            // Then
            assertThat(dto1).isEqualTo(dto2);
            assertThat(dto1).isNotEqualTo(dto3);
            assertThat(dto1.hashCode()).isEqualTo(dto2.hashCode());
        }
    }
}

