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
            assertThat(req)
                    .extracting(TagDTO.Req::getName, TagDTO.Req::getSlug)
                    .containsExactly("Java", "java");
        }

        @Test
        @DisplayName("Should create Req with no-args constructor")
        void shouldCreateReqWithNoArgsConstructor() {
            // When
            TagDTO.Req req = new TagDTO.Req();
            req.setName("Spring");
            req.setSlug("spring");

            // Then
            assertThat(req)
                    .extracting(TagDTO.Req::getName, TagDTO.Req::getSlug)
                    .containsExactly("Spring", "spring");
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
            assertThat(dto)
                    .extracting(TagDTO.TagSimpleDTO::getId, TagDTO.TagSimpleDTO::getName, TagDTO.TagSimpleDTO::getSlug, TagDTO.TagSimpleDTO::getPostCount)
                    .containsExactly(1, "Java", "java", 10);
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
            assertThat(dto)
                    .extracting(TagDTO.TagSimpleDTO::getId, TagDTO.TagSimpleDTO::getName, TagDTO.TagSimpleDTO::getSlug, TagDTO.TagSimpleDTO::getPostCount)
                    .containsExactly(2, "Spring", "spring", 5);
        }

        @Test
        @DisplayName("Should have working equals and hashCode")
        void shouldHaveWorkingEqualsAndHashCode() {
            // Given
            TagDTO.TagSimpleDTO dto1 = new TagDTO.TagSimpleDTO(1, "Java", "java", 10);
            TagDTO.TagSimpleDTO dto2 = new TagDTO.TagSimpleDTO(1, "Java", "java", 10);
            TagDTO.TagSimpleDTO dto3 = new TagDTO.TagSimpleDTO(2, "Spring", "spring", 5);

            // Then
            assertThat(dto1)
                    .isEqualTo(dto2)
                    .isNotEqualTo(dto3)
                    .hasSameHashCodeAs(dto2);
        }
    }
}

