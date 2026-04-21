package com.grummans.noyblog.services.client;

import com.grummans.noyblog.dto.CategoryDTO;
import com.grummans.noyblog.dto.PostDTO;
import com.grummans.noyblog.dto.TagDTO;
import com.grummans.noyblog.dto.UserDTO;
import com.grummans.noyblog.mapper.CategoryMapper;
import com.grummans.noyblog.mapper.PostMapper;
import com.grummans.noyblog.mapper.TagMapper;
import com.grummans.noyblog.mapper.UserMapper;
import com.grummans.noyblog.model.Categories;
import com.grummans.noyblog.model.Posts;
import com.grummans.noyblog.model.Tags;
import com.grummans.noyblog.model.Users;
import com.grummans.noyblog.repository.CategoryRepository;
import com.grummans.noyblog.repository.PostRepository;
import com.grummans.noyblog.repository.PostTagsRepository;
import com.grummans.noyblog.repository.UsersRepository;
import com.grummans.noyblog.services.system.FileService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ClientPostService Tests")
class ClientPostServiceTest {

    @Mock
    private FileService fileService;

    @Mock
    private PostRepository postRepository;

    @Mock
    private UsersRepository usersRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private PostTagsRepository postTagsRepository;

    @Mock
    private CategoryMapper categoryMapper;

    @Mock
    private UserMapper userMapper;

    @Mock
    private PostMapper postMapper;

    @Mock
    private TagMapper tagMapper;

    @InjectMocks
    private ClientPostService clientPostService;

    private Posts testPost;
    private Categories testCategory;
    private Users testAuthor;
    private Tags testTag;

    @BeforeEach
    void setUp() {
        testPost = Posts.builder()
                .id(1)
                .title("Test Post")
                .slug("test-post")
                .content("Test content")
                .excerpt("Test excerpt")
                .authorId(1)
                .categoryId(1)
                .status("PUBLISHED")
                .isFeatured(true)
                .publishedAt(LocalDateTime.now())
                .build();

        testCategory = Categories.builder()
                .id(1)
                .name("Technology")
                .slug("technology")
                .build();

        testAuthor = Users.builder()
                .id(1)
                .username("grummans")
                .displayName("Grummans")
                .build();

        testTag = Tags.builder()
                .id(1)
                .name("Java")
                .slug("java")
                .build();
    }

    @Nested
    @DisplayName("getAllPosts")
    class GetAllPostsTests {

        @Test
        @DisplayName("Should return all published posts")
        void shouldReturnAllPublishedPosts() {
            // Given
            List<Posts> posts = Arrays.asList(testPost);
            PostDTO.PostForClientDTO postDTO = new PostDTO.PostForClientDTO();
            postDTO.setId(1);
            postDTO.setTitle("Test Post");

            CategoryDTO.CategorySimpleDTO categoryDTO = new CategoryDTO.CategorySimpleDTO(1, "Technology", "technology", 0);
            TagDTO.TagSimpleDTO tagDTO = new TagDTO.TagSimpleDTO(1, "Java", "java", 0);

            when(postRepository.findAllByStatus("PUBLISHED")).thenReturn(posts);
            when(postMapper.toPostDTOForClient(testPost)).thenReturn(postDTO);
            when(categoryRepository.findById(1)).thenReturn(Optional.of(testCategory));
            when(categoryMapper.toCategorySimpleDTO(testCategory)).thenReturn(categoryDTO);
            when(postTagsRepository.findByPostId(1)).thenReturn(List.of(testTag));
            when(tagMapper.toTagSimpleDTO(testTag)).thenReturn(tagDTO);

            // When
            List<PostDTO.PostForClientDTO> result = clientPostService.getAllPosts();

            // Then
            assertThat(result).hasSize(1);
            assertThat(result.get(0).getTitle()).isEqualTo("Test Post");
            verify(postRepository).findAllByStatus("PUBLISHED");
        }

        @Test
        @DisplayName("Should return empty list when no published posts")
        void shouldReturnEmptyListWhenNoPublishedPosts() {
            // Given
            when(postRepository.findAllByStatus("PUBLISHED")).thenReturn(List.of());

            // When
            List<PostDTO.PostForClientDTO> result = clientPostService.getAllPosts();

            // Then
            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("getFeaturedPosts")
    class GetFeaturedPostsTests {

        @Test
        @DisplayName("Should return featured posts")
        void shouldReturnFeaturedPosts() {
            // Given
            List<Posts> posts = Arrays.asList(testPost);
            PostDTO.PostForClientDTO postDTO = new PostDTO.PostForClientDTO();
            postDTO.setId(1);
            postDTO.setTitle("Test Post");

            CategoryDTO.CategorySimpleDTO categoryDTO = new CategoryDTO.CategorySimpleDTO(1, "Technology", "technology", 0);

            when(postRepository.findByStatusAndIsFeatured("PUBLISHED", true)).thenReturn(posts);
            when(postMapper.toPostDTOForClient(testPost)).thenReturn(postDTO);
            when(categoryRepository.findById(1)).thenReturn(Optional.of(testCategory));
            when(categoryMapper.toCategorySimpleDTO(testCategory)).thenReturn(categoryDTO);
            when(postTagsRepository.findByPostId(1)).thenReturn(List.of());

            // When
            List<PostDTO.PostForClientDTO> result = clientPostService.getFeaturedPosts();

            // Then
            assertThat(result).hasSize(1);
            verify(postRepository).findByStatusAndIsFeatured("PUBLISHED", true);
        }
    }

    @Nested
    @DisplayName("getDetailPost")
    class GetDetailPostTests {

        @Test
        @DisplayName("Should return post detail when found")
        void shouldReturnPostDetailWhenFound() {
            // Given
            PostDTO.Res postDTO = new PostDTO.Res();
            postDTO.setId(1);
            postDTO.setTitle("Test Post");

            CategoryDTO.CategorySimpleDTO categoryDTO = new CategoryDTO.CategorySimpleDTO(1, "Technology", "technology", 0);
            UserDTO.AuthorDTO authorDTO = new UserDTO.AuthorDTO(1, "grummans", "Grummans");

            when(postRepository.findById(1)).thenReturn(Optional.of(testPost));
            when(postMapper.toPostDTO(testPost)).thenReturn(postDTO);
            when(usersRepository.findById(1)).thenReturn(Optional.of(testAuthor));
            when(userMapper.toAuthorDTO(testAuthor)).thenReturn(authorDTO);
            when(categoryRepository.findById(1)).thenReturn(Optional.of(testCategory));
            when(categoryMapper.toCategorySimpleDTO(testCategory)).thenReturn(categoryDTO);
            when(postTagsRepository.findByPostId(1)).thenReturn(List.of());
            when(fileService.getPostAttachments(1)).thenReturn(List.of());

            // When
            PostDTO.Res result = clientPostService.getDetailPost(1);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getTitle()).isEqualTo("Test Post");
        }

        @Test
        @DisplayName("Should throw exception when post not found")
        void shouldThrowExceptionWhenPostNotFound() {
            // Given
            when(postRepository.findById(999)).thenReturn(Optional.empty());

            // When/Then
            assertThatThrownBy(() -> clientPostService.getDetailPost(999))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Post not found with id: 999");
        }
    }

    @Nested
    @DisplayName("getDetailPostBySlug")
    class GetDetailPostBySlugTests {

        @Test
        @DisplayName("Should return post detail when found by slug")
        void shouldReturnPostDetailWhenFoundBySlug() {
            // Given
            PostDTO.Res postDTO = new PostDTO.Res();
            postDTO.setId(1);
            postDTO.setTitle("Test Post");

            CategoryDTO.CategorySimpleDTO categoryDTO = new CategoryDTO.CategorySimpleDTO(1, "Technology", "technology", 0);
            UserDTO.AuthorDTO authorDTO = new UserDTO.AuthorDTO(1, "grummans", "Grummans");

            when(postRepository.findBySlug("test-post")).thenReturn(testPost);
            when(postMapper.toPostDTO(testPost)).thenReturn(postDTO);
            when(usersRepository.findById(1)).thenReturn(Optional.of(testAuthor));
            when(userMapper.toAuthorDTO(testAuthor)).thenReturn(authorDTO);
            when(categoryRepository.findById(1)).thenReturn(Optional.of(testCategory));
            when(categoryMapper.toCategorySimpleDTO(testCategory)).thenReturn(categoryDTO);
            when(postTagsRepository.findByPostId(1)).thenReturn(List.of());
            when(fileService.getPostAttachments(1)).thenReturn(List.of());

            // When
            PostDTO.Res result = clientPostService.getDetailPostBySlug("test-post");

            // Then
            assertThat(result).isNotNull();
        }

        @Test
        @DisplayName("Should throw exception when post not found by slug")
        void shouldThrowExceptionWhenPostNotFoundBySlug() {
            // Given
            when(postRepository.findBySlug("non-existent")).thenReturn(null);

            // When/Then
            assertThatThrownBy(() -> clientPostService.getDetailPostBySlug("non-existent"))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Post not found with slug: non-existent");
        }
    }
}

