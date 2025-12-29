package com.grummans.noyblog.services.admin;

import com.grummans.noyblog.dto.TagDTO;
import com.grummans.noyblog.mapper.TagMapper;
import com.grummans.noyblog.model.Tags;
import com.grummans.noyblog.repository.PostTagsRepository;
import com.grummans.noyblog.repository.TagsRepository;
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
@DisplayName("AdminTagService Tests")
class AdminTagServiceTest {

    @Mock
    private TagsRepository tagsRepository;

    @Mock
    private PostTagsRepository postTagsRepository;

    @Mock
    private TagMapper tagMapper;

    @InjectMocks
    private AdminTagService adminTagService;

    private Tags testTag;
    private TagDTO.TagSimpleDTO testTagDTO;
    private TagDTO.Req testTagReq;

    @BeforeEach
    void setUp() {
        testTag = Tags.builder()
                .id(1)
                .name("Java")
                .slug("java")
                .build();

        testTagDTO = new TagDTO.TagSimpleDTO(1, "Java", "java", 5);
        testTagReq = new TagDTO.Req("Java", "java");
    }

    @Nested
    @DisplayName("getAllTags")
    class GetAllTagsTests {

        @Test
        @DisplayName("Should return all tags with post count")
        void shouldReturnAllTagsWithPostCount() {
            // Given
            Tags tag1 = Tags.builder().id(1).name("Java").slug("java").build();
            Tags tag2 = Tags.builder().id(2).name("Spring").slug("spring").build();
            List<Tags> tags = Arrays.asList(tag1, tag2);

            TagDTO.TagSimpleDTO dto1 = new TagDTO.TagSimpleDTO(1, "Java", "java", 0);
            TagDTO.TagSimpleDTO dto2 = new TagDTO.TagSimpleDTO(2, "Spring", "spring", 0);

            when(tagsRepository.findAll()).thenReturn(tags);
            when(tagMapper.toTagSimpleDTO(tag1)).thenReturn(dto1);
            when(tagMapper.toTagSimpleDTO(tag2)).thenReturn(dto2);
            when(postTagsRepository.countByTagId(1)).thenReturn(5);
            when(postTagsRepository.countByTagId(2)).thenReturn(3);

            // When
            List<TagDTO.TagSimpleDTO> result = adminTagService.getAllTags();

            // Then
            assertThat(result).hasSize(2);
            assertThat(result.get(0).getPostCount()).isEqualTo(5);
            assertThat(result.get(1).getPostCount()).isEqualTo(3);
            verify(tagsRepository).findAll();
        }

        @Test
        @DisplayName("Should return empty list when no tags exist")
        void shouldReturnEmptyListWhenNoTagsExist() {
            // Given
            when(tagsRepository.findAll()).thenReturn(List.of());

            // When
            List<TagDTO.TagSimpleDTO> result = adminTagService.getAllTags();

            // Then
            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("createTag")
    class CreateTagTests {

        @Test
        @DisplayName("Should create tag successfully")
        void shouldCreateTagSuccessfully() {
            // Given
            when(tagMapper.toTag(testTagReq)).thenReturn(testTag);
            when(tagsRepository.save(testTag)).thenReturn(testTag);
            when(tagMapper.toTagSimpleDTO(testTag)).thenReturn(testTagDTO);

            // When
            TagDTO.TagSimpleDTO result = adminTagService.createTag(testTagReq);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getName()).isEqualTo("Java");
            assertThat(result.getSlug()).isEqualTo("java");
            verify(tagsRepository).save(any(Tags.class));
        }
    }

    @Nested
    @DisplayName("getDetailTag")
    class GetDetailTagTests {

        @Test
        @DisplayName("Should return tag when found")
        void shouldReturnTagWhenFound() {
            // Given
            when(tagsRepository.findById(1)).thenReturn(Optional.of(testTag));
            when(tagMapper.toTagSimpleDTO(testTag)).thenReturn(testTagDTO);

            // When
            TagDTO.TagSimpleDTO result = adminTagService.getDetailTag(1);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(1);
            assertThat(result.getName()).isEqualTo("Java");
        }

        @Test
        @DisplayName("Should throw exception when tag not found")
        void shouldThrowExceptionWhenTagNotFound() {
            // Given
            when(tagsRepository.findById(999)).thenReturn(Optional.empty());

            // When/Then
            assertThatThrownBy(() -> adminTagService.getDetailTag(999))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("Tag not found with id: 999");
        }
    }

    @Nested
    @DisplayName("updateTag")
    class UpdateTagTests {

        @Test
        @DisplayName("Should update tag successfully")
        void shouldUpdateTagSuccessfully() {
            // Given
            TagDTO.Req updateReq = new TagDTO.Req("Updated Java", "updated-java");
            Tags updatedTag = Tags.builder()
                    .id(1)
                    .name("Updated Java")
                    .slug("updated-java")
                    .build();
            TagDTO.TagSimpleDTO updatedDTO = new TagDTO.TagSimpleDTO(1, "Updated Java", "updated-java", 5);

            when(tagsRepository.findById(1)).thenReturn(Optional.of(testTag));
            when(tagsRepository.save(testTag)).thenReturn(updatedTag);
            when(tagMapper.toTagSimpleDTO(updatedTag)).thenReturn(updatedDTO);

            // When
            TagDTO.TagSimpleDTO result = adminTagService.updateTag(1, updateReq);

            // Then
            assertThat(result.getName()).isEqualTo("Updated Java");
            assertThat(result.getSlug()).isEqualTo("updated-java");
            verify(tagsRepository).save(testTag);
        }

        @Test
        @DisplayName("Should throw exception when updating non-existent tag")
        void shouldThrowExceptionWhenUpdatingNonExistentTag() {
            // Given
            when(tagsRepository.findById(999)).thenReturn(Optional.empty());

            // When/Then
            assertThatThrownBy(() -> adminTagService.updateTag(999, testTagReq))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("Tag not found with id: 999");
        }
    }

    @Nested
    @DisplayName("deleteTag")
    class DeleteTagTests {

        @Test
        @DisplayName("Should delete tag successfully")
        void shouldDeleteTagSuccessfully() {
            // Given
            when(tagsRepository.findById(1)).thenReturn(Optional.of(testTag));
            doNothing().when(tagsRepository).delete(testTag);

            // When
            adminTagService.deleteTag(1);

            // Then
            verify(tagsRepository).delete(testTag);
        }

        @Test
        @DisplayName("Should throw exception when deleting non-existent tag")
        void shouldThrowExceptionWhenDeletingNonExistentTag() {
            // Given
            when(tagsRepository.findById(999)).thenReturn(Optional.empty());

            // When/Then
            assertThatThrownBy(() -> adminTagService.deleteTag(999))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("Tag not found with id: 999");
        }
    }
}

