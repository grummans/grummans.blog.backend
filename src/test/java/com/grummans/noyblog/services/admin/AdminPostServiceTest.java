package com.grummans.noyblog.services.admin;

import com.grummans.noyblog.dto.PostDTO;
import com.grummans.noyblog.mapper.*;
import com.grummans.noyblog.model.*;
import com.grummans.noyblog.repository.*;
import com.grummans.noyblog.services.system.ContentService;
import com.grummans.noyblog.services.system.FileService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AdminPostService Tests")
class AdminPostServiceTest {

    @Mock
    private PostRepository postRepository;

    @Mock
    private PostTagsRepository postTagsRepository;

    @Mock
    private UsersRepository usersRepository;

    @Mock
    private TagsRepository tagsRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private PostMapper postMapper;

    @Mock
    private UserMapper userMapper;

    @Mock
    private CategoryMapper categoryMapper;

    @SuppressWarnings("unused") // Required for @InjectMocks
    @Mock
    private TagMapper tagMapper;

    @Mock
    private FileService fileService;

    @Mock
    private ContentService contentService;

    @InjectMocks
    private AdminPostService adminPostService;

    private Posts testPost;
    private Users testUser;
    private Categories testCategory;
    private PostDTO.Res testPostRes;
    private PostDTO.Req testPostReq;

    @BeforeEach
    void setUp() {
        testUser = new Users();
        testUser.setId(1);
        testUser.setUsername("grummans");
        testUser.setDisplayName("Grummans");

        testCategory = new Categories();
        testCategory.setId(1);
        testCategory.setName("Technology");
        testCategory.setSlug("technology");


        testPost = new Posts();
        testPost.setId(1);
        testPost.setTitle("Test Post");
        testPost.setSlug("test-post");
        testPost.setContent("<p>Test content</p>");
        testPost.setContentHtml("<p>Test content</p>");
        testPost.setStatus("PUBLISHED");
        testPost.setAuthorId(1);
        testPost.setCategoryId(1);
        testPost.setCreatedAt(LocalDateTime.now());

        testPostRes = new PostDTO.Res();
        testPostRes.setId(1);
        testPostRes.setTitle("Test Post");
        testPostRes.setSlug("test-post");

        testPostReq = new PostDTO.Req();
        testPostReq.setTitle("Test Post");
        testPostReq.setSlug("test-post");
        testPostReq.setContent("<p>Test content</p>");
        testPostReq.setCategoryId(1);
        testPostReq.setTagId(List.of(1));
    }

    @Nested
    @DisplayName("getAllPost Tests")
    class GetAllPostTests {

        @Test
        @DisplayName("Should return all posts with pagination")
        void shouldReturnAllPostsWithPagination() {
            // Given
            Page<Posts> postsPage = new PageImpl<>(List.of(testPost));
            when(postRepository.findAll(any(Pageable.class))).thenReturn(postsPage);
            when(postMapper.toPostDTO(any(Posts.class))).thenReturn(testPostRes);
            when(usersRepository.findById(1)).thenReturn(Optional.of(testUser));
            when(userMapper.toAuthorDTO(any(Users.class))).thenReturn(new com.grummans.noyblog.dto.UserDTO.AuthorDTO());
            when(categoryRepository.findById(1)).thenReturn(Optional.of(testCategory));
            when(categoryMapper.toCategorySimpleDTO(any(Categories.class))).thenReturn(new com.grummans.noyblog.dto.CategoryDTO.CategorySimpleDTO(1, "Tech", "tech", 0));
            when(postTagsRepository.findByPostId(1)).thenReturn(new ArrayList<>());

            PostDTO.Req req = new PostDTO.Req();

            // When
            Page<PostDTO.Res> result = adminPostService.getAllPost(req, 0, 10);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getContent()).hasSize(1);
            verify(postRepository).findAll(any(Pageable.class));
        }

        @Test
        @DisplayName("Should filter posts by title")
        void shouldFilterPostsByTitle() {
            // Given
            Page<Posts> postsPage = new PageImpl<>(List.of(testPost));
            when(postRepository.findByTitleContainingIgnoreCase(eq("Test"), any(Pageable.class))).thenReturn(postsPage);
            when(postMapper.toPostDTO(any(Posts.class))).thenReturn(testPostRes);
            when(usersRepository.findById(1)).thenReturn(Optional.of(testUser));
            when(userMapper.toAuthorDTO(any(Users.class))).thenReturn(new com.grummans.noyblog.dto.UserDTO.AuthorDTO());
            when(categoryRepository.findById(1)).thenReturn(Optional.of(testCategory));
            when(categoryMapper.toCategorySimpleDTO(any(Categories.class))).thenReturn(new com.grummans.noyblog.dto.CategoryDTO.CategorySimpleDTO(1, "Tech", "tech", 0));
            when(postTagsRepository.findByPostId(1)).thenReturn(new ArrayList<>());

            PostDTO.Req req = new PostDTO.Req();
            req.setTitle("Test");

            // When
            Page<PostDTO.Res> result = adminPostService.getAllPost(req, 0, 10);

            // Then
            assertThat(result).isNotNull();
            verify(postRepository).findByTitleContainingIgnoreCase(eq("Test"), any(Pageable.class));
        }

        @Test
        @DisplayName("Should filter posts by status")
        void shouldFilterPostsByStatus() {
            // Given
            Page<Posts> postsPage = new PageImpl<>(List.of(testPost));
            when(postRepository.findByStatus(eq("PUBLISHED"), any(Pageable.class))).thenReturn(postsPage);
            when(postMapper.toPostDTO(any(Posts.class))).thenReturn(testPostRes);
            when(usersRepository.findById(1)).thenReturn(Optional.of(testUser));
            when(userMapper.toAuthorDTO(any(Users.class))).thenReturn(new com.grummans.noyblog.dto.UserDTO.AuthorDTO());
            when(categoryRepository.findById(1)).thenReturn(Optional.of(testCategory));
            when(categoryMapper.toCategorySimpleDTO(any(Categories.class))).thenReturn(new com.grummans.noyblog.dto.CategoryDTO.CategorySimpleDTO(1, "Tech", "tech", 0));
            when(postTagsRepository.findByPostId(1)).thenReturn(new ArrayList<>());

            PostDTO.Req req = new PostDTO.Req();
            req.setStatus("PUBLISHED");

            // When
            Page<PostDTO.Res> result = adminPostService.getAllPost(req, 0, 10);

            // Then
            assertThat(result).isNotNull();
            verify(postRepository).findByStatus(eq("PUBLISHED"), any(Pageable.class));
        }

        @Test
        @DisplayName("Should filter posts by title and status")
        void shouldFilterPostsByTitleAndStatus() {
            // Given
            Page<Posts> postsPage = new PageImpl<>(List.of(testPost));
            when(postRepository.findByTitleContainingIgnoreCaseAndStatus(eq("Test"), eq("PUBLISHED"), any(Pageable.class))).thenReturn(postsPage);
            when(postMapper.toPostDTO(any(Posts.class))).thenReturn(testPostRes);
            when(usersRepository.findById(1)).thenReturn(Optional.of(testUser));
            when(userMapper.toAuthorDTO(any(Users.class))).thenReturn(new com.grummans.noyblog.dto.UserDTO.AuthorDTO());
            when(categoryRepository.findById(1)).thenReturn(Optional.of(testCategory));
            when(categoryMapper.toCategorySimpleDTO(any(Categories.class))).thenReturn(new com.grummans.noyblog.dto.CategoryDTO.CategorySimpleDTO(1, "Tech", "tech", 0));
            when(postTagsRepository.findByPostId(1)).thenReturn(new ArrayList<>());

            PostDTO.Req req = new PostDTO.Req();
            req.setTitle("Test");
            req.setStatus("PUBLISHED");

            // When
            Page<PostDTO.Res> result = adminPostService.getAllPost(req, 0, 10);

            // Then
            assertThat(result).isNotNull();
            verify(postRepository).findByTitleContainingIgnoreCaseAndStatus(eq("Test"), eq("PUBLISHED"), any(Pageable.class));
        }
    }

    @Nested
    @DisplayName("detailPost Tests")
    class DetailPostTests {

        @Test
        @DisplayName("Should return post detail by id")
        void shouldReturnPostDetailById() {
            // Given
            when(postRepository.findById(1)).thenReturn(Optional.of(testPost));
            when(postMapper.toPostDTO(any(Posts.class))).thenReturn(testPostRes);
            when(usersRepository.findById(1)).thenReturn(Optional.of(testUser));
            when(userMapper.toAuthorDTO(any(Users.class))).thenReturn(new com.grummans.noyblog.dto.UserDTO.AuthorDTO());
            when(categoryRepository.findById(1)).thenReturn(Optional.of(testCategory));
            when(categoryMapper.toCategorySimpleDTO(any(Categories.class))).thenReturn(new com.grummans.noyblog.dto.CategoryDTO.CategorySimpleDTO(1, "Tech", "tech", 0));
            when(postTagsRepository.findByPostId(1)).thenReturn(new ArrayList<>());
            when(fileService.getPostAttachments(1)).thenReturn(new ArrayList<>());

            // When
            PostDTO.Res result = adminPostService.detailPost(1);

            // Then
            assertThat(result).isNotNull();
            verify(postRepository).findById(1);
        }

        @Test
        @DisplayName("Should throw exception when post not found")
        void shouldThrowExceptionWhenPostNotFound() {
            // Given
            when(postRepository.findById(999)).thenReturn(Optional.empty());

            // When/Then
            assertThatThrownBy(() -> adminPostService.detailPost(999))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Post not found");
        }

        @Test
        @DisplayName("Should return post detail for editing")
        void shouldReturnPostDetailForEditing() {
            // Given
            when(postRepository.findById(1)).thenReturn(Optional.of(testPost));
            when(postMapper.toPostDTO(any(Posts.class))).thenReturn(testPostRes);
            when(usersRepository.findById(1)).thenReturn(Optional.of(testUser));
            when(userMapper.toAuthorDTO(any(Users.class))).thenReturn(new com.grummans.noyblog.dto.UserDTO.AuthorDTO());
            when(categoryRepository.findById(1)).thenReturn(Optional.of(testCategory));
            when(categoryMapper.toCategorySimpleDTO(any(Categories.class))).thenReturn(new com.grummans.noyblog.dto.CategoryDTO.CategorySimpleDTO(1, "Tech", "tech", 0));
            when(postTagsRepository.findByPostId(1)).thenReturn(new ArrayList<>());
            when(fileService.getPostAttachments(1)).thenReturn(new ArrayList<>());

            // When
            PostDTO.Res result = adminPostService.detailPostForEdit(1);

            // Then
            assertThat(result).isNotNull();
            verify(postRepository).findById(1);
        }
    }

    @Nested
    @DisplayName("deletePost Tests")
    class DeletePostTests {

        @Test
        @DisplayName("Should delete post by id")
        void shouldDeletePostById() {
            // Given
            when(postRepository.findById(1)).thenReturn(Optional.of(testPost));
            doNothing().when(fileService).deletePostFile(1);
            doNothing().when(postRepository).delete(any(Posts.class));

            // When
            adminPostService.deletePost(1);

            // Then
            verify(postRepository).findById(1);
            verify(fileService).deletePostFile(1);
            verify(postRepository).delete(testPost);
        }

        @Test
        @DisplayName("Should throw exception when deleting non-existent post")
        void shouldThrowExceptionWhenDeletingNonExistentPost() {
            // Given
            when(postRepository.findById(999)).thenReturn(Optional.empty());

            // When/Then
            assertThatThrownBy(() -> adminPostService.deletePost(999))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Post not found");
        }
    }

    @Nested
    @DisplayName("saveDraftPost Tests")
    class SaveDraftPostTests {

        @Test
        @DisplayName("Should create new draft post")
        void shouldCreateNewDraftPost() {
            // Given
            PostDTO.Req req = new PostDTO.Req();
            req.setTitle("Draft Post");
            req.setContent("<p>Draft content</p>");

            Posts draftPost = new Posts();
            draftPost.setId(2);
            draftPost.setTitle("Draft Post");
            draftPost.setStatus("DRAFT");

            PostDTO.SimplePostDTO simplePostDTO = new PostDTO.SimplePostDTO();
            simplePostDTO.setId(2);

            when(contentService.sanitizeHtml(anyString())).thenReturn("<p>Draft content</p>");
            when(postMapper.toPost(any(PostDTO.Req.class))).thenReturn(draftPost);
            when(usersRepository.findIdByUsername("grummans")).thenReturn(1);
            when(postRepository.save(any(Posts.class))).thenReturn(draftPost);
            when(fileService.extractFileUrlsFromContent(anyString())).thenReturn(new ArrayList<>());
            when(postMapper.toSimplePostDTO(any(Posts.class))).thenReturn(simplePostDTO);

            // When
            PostDTO.SimplePostDTO result = adminPostService.saveDraftPost(req, null);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(2);
            verify(postRepository).save(any(Posts.class));
        }

        @Test
        @DisplayName("Should update existing draft post")
        void shouldUpdateExistingDraftPost() {
            // Given
            PostDTO.Req req = new PostDTO.Req();
            req.setId(1);
            req.setTitle("Updated Draft");
            req.setContent("<p>Updated content</p>");

            Posts existingDraft = new Posts();
            existingDraft.setId(1);
            existingDraft.setStatus("DRAFT");

            PostDTO.SimplePostDTO simplePostDTO = new PostDTO.SimplePostDTO();
            simplePostDTO.setId(1);

            when(postRepository.findById(1)).thenReturn(Optional.of(existingDraft));
            when(contentService.sanitizeHtml(anyString())).thenReturn("<p>Updated content</p>");
            when(postRepository.save(any(Posts.class))).thenReturn(existingDraft);
            when(fileService.extractFileUrlsFromContent(anyString())).thenReturn(new ArrayList<>());
            when(postMapper.toSimplePostDTO(any(Posts.class))).thenReturn(simplePostDTO);

            // When
            PostDTO.SimplePostDTO result = adminPostService.saveDraftPost(req, null);

            // Then
            assertThat(result).isNotNull();
            verify(postRepository).findById(1);
            verify(postRepository).save(any(Posts.class));
        }

        @Test
        @DisplayName("Should throw exception when trying to save published post as draft")
        void shouldThrowExceptionWhenSavingPublishedPostAsDraft() {
            // Given
            PostDTO.Req req = new PostDTO.Req();
            req.setId(1);
            req.setTitle("Test");
            req.setContent("<p>Test</p>");

            Posts publishedPost = new Posts();
            publishedPost.setId(1);
            publishedPost.setStatus("PUBLISHED");

            when(contentService.sanitizeHtml(anyString())).thenReturn("<p>Test</p>");
            when(postRepository.findById(1)).thenReturn(Optional.of(publishedPost));

            // When/Then
            assertThatThrownBy(() -> adminPostService.saveDraftPost(req, null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Cannot save as draft");
        }
    }

    @Nested
    @DisplayName("saveAndPublishPost Tests")
    class SaveAndPublishPostTests {

        @Test
        @DisplayName("Should publish new post successfully")
        void shouldPublishNewPostSuccessfully() {
            // Given
            PostDTO.Req req = new PostDTO.Req();
            req.setTitle("New Post");
            req.setSlug("new-post");
            req.setContent("<p>Content</p>");
            req.setCategoryId(1);
            req.setTagId(List.of(1));
            req.setAuthorUsername("grummans");

            Posts newPost = new Posts();
            newPost.setId(1);
            newPost.setTitle("New Post");
            newPost.setStatus("PUBLISHED");

            PostDTO.SimplePostDTO simplePostDTO = new PostDTO.SimplePostDTO();
            simplePostDTO.setId(1);

            when(contentService.sanitizeHtml(anyString())).thenReturn("<p>Content</p>");
            when(postMapper.toPost(any(PostDTO.Req.class))).thenReturn(newPost);
            when(usersRepository.findIdByUsername("grummans")).thenReturn(1);
            when(postRepository.save(any(Posts.class))).thenReturn(newPost);
            when(postRepository.findById(1)).thenReturn(Optional.of(newPost)); // For updatePostTags
            when(fileService.extractFileUrlsFromContent(anyString())).thenReturn(new ArrayList<>());
            when(fileService.moveContentFilesToPost(anyInt(), anyList())).thenReturn(new java.util.HashMap<>());
            when(tagsRepository.findById(1)).thenReturn(Optional.of(new Tags()));
            when(postMapper.toSimplePostDTO(any(Posts.class))).thenReturn(simplePostDTO);

            // When
            PostDTO.SimplePostDTO result = adminPostService.saveAndPublishPost(req, null);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(1);
            verify(postRepository, atLeastOnce()).save(any(Posts.class));
        }

        @Test
        @DisplayName("Should publish existing draft successfully")
        void shouldPublishExistingDraftSuccessfully() {
            // Given
            PostDTO.Req req = new PostDTO.Req();
            req.setId(1);
            req.setTitle("Draft to Publish");
            req.setSlug("draft-to-publish");
            req.setContent("<p>Content</p>");
            req.setCategoryId(1);
            req.setTagId(List.of(1));

            Posts draftPost = new Posts();
            draftPost.setId(1);
            draftPost.setStatus("DRAFT");

            PostDTO.SimplePostDTO simplePostDTO = new PostDTO.SimplePostDTO();
            simplePostDTO.setId(1);

            when(contentService.sanitizeHtml(anyString())).thenReturn("<p>Content</p>");
            when(postRepository.findById(1)).thenReturn(Optional.of(draftPost));
            when(postRepository.save(any(Posts.class))).thenReturn(draftPost);
            when(fileService.extractFileUrlsFromContent(anyString())).thenReturn(new ArrayList<>());
            when(fileService.moveContentFilesToPost(anyInt(), anyList())).thenReturn(new java.util.HashMap<>());
            when(tagsRepository.findById(1)).thenReturn(Optional.of(new Tags()));
            when(postMapper.toSimplePostDTO(any(Posts.class))).thenReturn(simplePostDTO);

            // When
            PostDTO.SimplePostDTO result = adminPostService.saveAndPublishPost(req, null);

            // Then
            assertThat(result).isNotNull();
            verify(postRepository, atLeastOnce()).findById(1);
            verify(postRepository, atLeastOnce()).save(any(Posts.class));
        }

        @Test
        @DisplayName("Should throw exception when publishing already published post")
        void shouldThrowExceptionWhenPublishingAlreadyPublishedPost() {
            // Given
            PostDTO.Req req = new PostDTO.Req();
            req.setId(1);
            req.setTitle("Published Post");
            req.setSlug("published-post");
            req.setCategoryId(1);
            req.setTagId(List.of(1));

            Posts publishedPost = new Posts();
            publishedPost.setId(1);
            publishedPost.setStatus("PUBLISHED");

            when(postRepository.findById(1)).thenReturn(Optional.of(publishedPost));

            // When/Then
            assertThatThrownBy(() -> adminPostService.saveAndPublishPost(req, null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Cannot publish");
        }

        @Test
        @DisplayName("Should throw exception when title is missing")
        void shouldThrowExceptionWhenTitleIsMissing() {
            // Given
            PostDTO.Req req = new PostDTO.Req();
            req.setSlug("test-slug");
            req.setCategoryId(1);
            req.setTagId(List.of(1));

            // When/Then
            assertThatThrownBy(() -> adminPostService.saveAndPublishPost(req, null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Title is required");
        }

        @Test
        @DisplayName("Should throw exception when slug is missing")
        void shouldThrowExceptionWhenSlugIsMissing() {
            // Given
            PostDTO.Req req = new PostDTO.Req();
            req.setTitle("Test Title");
            req.setCategoryId(1);
            req.setTagId(List.of(1));

            // When/Then
            assertThatThrownBy(() -> adminPostService.saveAndPublishPost(req, null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Slug is required");
        }

        @Test
        @DisplayName("Should throw exception when category is missing")
        void shouldThrowExceptionWhenCategoryIsMissing() {
            // Given
            PostDTO.Req req = new PostDTO.Req();
            req.setTitle("Test Title");
            req.setSlug("test-slug");
            req.setTagId(List.of(1));

            // When/Then
            assertThatThrownBy(() -> adminPostService.saveAndPublishPost(req, null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Category is required");
        }

        @Test
        @DisplayName("Should throw exception when tags are missing")
        void shouldThrowExceptionWhenTagsAreMissing() {
            // Given
            PostDTO.Req req = new PostDTO.Req();
            req.setTitle("Test Title");
            req.setSlug("test-slug");
            req.setCategoryId(1);

            // When/Then
            assertThatThrownBy(() -> adminPostService.saveAndPublishPost(req, null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("tag is required");
        }
    }

    @Nested
    @DisplayName("updatePublishedPost Tests")
    class UpdatePublishedPostTests {

        @Test
        @DisplayName("Should update published post successfully")
        void shouldUpdatePublishedPostSuccessfully() {
            // Given
            Posts publishedPost = new Posts();
            publishedPost.setId(1);
            publishedPost.setStatus("PUBLISHED");
            publishedPost.setContent("<p>Old content</p>");

            PostDTO.SimplePostDTO simplePostDTO = new PostDTO.SimplePostDTO();
            simplePostDTO.setId(1);

            when(postRepository.findById(1)).thenReturn(Optional.of(publishedPost));
            when(fileService.extractFileUrlsFromContent(anyString())).thenReturn(new ArrayList<>());
            when(contentService.sanitizeHtml(anyString())).thenReturn("<p>New content</p>");
            when(postRepository.save(any(Posts.class))).thenReturn(publishedPost);
            when(fileService.moveContentFilesToPost(anyInt(), anyList())).thenReturn(new java.util.HashMap<>());
            when(tagsRepository.findById(1)).thenReturn(Optional.of(new Tags()));
            when(postMapper.toSimplePostDTO(any(Posts.class))).thenReturn(simplePostDTO);

            // When
            PostDTO.SimplePostDTO result = adminPostService.updatePublishedPost(1, testPostReq, null);

            // Then
            assertThat(result).isNotNull();
            verify(postRepository).save(any(Posts.class));
        }

        @Test
        @DisplayName("Should throw exception when updating non-published post")
        void shouldThrowExceptionWhenUpdatingNonPublishedPost() {
            // Given
            Posts draftPost = new Posts();
            draftPost.setId(1);
            draftPost.setStatus("DRAFT");

            when(postRepository.findById(1)).thenReturn(Optional.of(draftPost));

            // When/Then
            assertThatThrownBy(() -> adminPostService.updatePublishedPost(1, testPostReq, null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Cannot update - post is not published");
        }

        @Test
        @DisplayName("Should throw exception when post not found")
        void shouldThrowExceptionWhenPostNotFound() {
            // Given
            when(postRepository.findById(999)).thenReturn(Optional.empty());

            // When/Then
            assertThatThrownBy(() -> adminPostService.updatePublishedPost(999, testPostReq, null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Post not found");
        }
    }
}

