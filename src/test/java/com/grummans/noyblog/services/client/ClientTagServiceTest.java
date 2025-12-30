package com.grummans.noyblog.services.client;

import com.grummans.noyblog.dto.TagDTO;
import com.grummans.noyblog.mapper.TagMapper;
import com.grummans.noyblog.model.Tags;
import com.grummans.noyblog.repository.PostTagsRepository;
import com.grummans.noyblog.repository.TagsRepository;
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
@DisplayName("ClientTagService Tests")
class ClientTagServiceTest {

    @Mock
    private TagsRepository tagsRepository;

    @Mock
    private PostTagsRepository postTagsRepository;

    @Mock
    private TagMapper tagMapper;

    @InjectMocks
    private ClientTagService clientTagService;

    private Tags testTag1;
    private Tags testTag2;

    @BeforeEach
    void setUp() {
        testTag1 = Tags.builder()
                .id(1)
                .name("Java")
                .slug("java")
                .build();

        testTag2 = Tags.builder()
                .id(2)
                .name("Spring")
                .slug("spring")
                .build();
    }

    @Test
    @DisplayName("Should return all tags with post count")
    void shouldReturnAllTagsWithPostCount() {
        // Given
        List<Tags> tags = Arrays.asList(testTag1, testTag2);

        TagDTO.TagSimpleDTO dto1 = new TagDTO.TagSimpleDTO(1, "Java", "java", 0);
        TagDTO.TagSimpleDTO dto2 = new TagDTO.TagSimpleDTO(2, "Spring", "spring", 0);

        when(tagsRepository.findAll()).thenReturn(tags);
        when(tagMapper.toTagSimpleDTO(testTag1)).thenReturn(dto1);
        when(tagMapper.toTagSimpleDTO(testTag2)).thenReturn(dto2);
        when(postTagsRepository.countByTagId(1)).thenReturn(10);
        when(postTagsRepository.countByTagId(2)).thenReturn(5);

        // When
        List<TagDTO.TagSimpleDTO> result = clientTagService.getAllTags();

        // Then
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getPostCount()).isEqualTo(10);
        assertThat(result.get(1).getPostCount()).isEqualTo(5);
        verify(tagsRepository).findAll();
        verify(postTagsRepository, times(2)).countByTagId(anyInt());
    }

    @Test
    @DisplayName("Should return empty list when no tags exist")
    void shouldReturnEmptyListWhenNoTagsExist() {
        // Given
        when(tagsRepository.findAll()).thenReturn(List.of());

        // When
        List<TagDTO.TagSimpleDTO> result = clientTagService.getAllTags();

        // Then
        assertThat(result).isEmpty();
        verify(tagsRepository).findAll();
        verify(postTagsRepository, never()).countByTagId(anyInt());
    }
}

