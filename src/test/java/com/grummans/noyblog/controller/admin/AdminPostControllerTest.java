package com.grummans.noyblog.controller.admin;

import com.grummans.noyblog.dto.PostDTO;
import com.grummans.noyblog.services.admin.AdminPostService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(
        controllers = AdminPostController.class,
        excludeAutoConfiguration = {SecurityAutoConfiguration.class},
        excludeFilters = @ComponentScan.Filter(type = FilterType.REGEX, pattern = "com.grummans.noyblog.configuration.*")
)
@DisplayName("AdminPostController Tests")
class AdminPostControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AdminPostService adminPostService;

    private PostDTO.Res testPostRes;

    @BeforeEach
    void setUp() {
        testPostRes = new PostDTO.Res();
        testPostRes.setId(1);
        testPostRes.setTitle("Test Post");
        testPostRes.setSlug("test-post");
        testPostRes.setStatus("PUBLISHED");
    }

    @Nested
    @DisplayName("GET /a/posts/list Tests")
    class GetAllPostsTests {

        @Test
        @DisplayName("Should return all posts with pagination")
        void shouldReturnAllPostsWithPagination() throws Exception {
            // Given
            Page<PostDTO.Res> postsPage = new PageImpl<>(List.of(testPostRes));
            when(adminPostService.getAllPost(any(PostDTO.Req.class), anyInt(), anyInt())).thenReturn(postsPage);

            // When/Then
            mockMvc.perform(get("/a/posts/list")
                            .param("page", "1")
                            .param("size", "10"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code", is(200)))
                    .andExpect(jsonPath("$.data.content", hasSize(1)))
                    .andExpect(jsonPath("$.data.content[0].title", is("Test Post")));

            verify(adminPostService).getAllPost(any(PostDTO.Req.class), eq(0), eq(10));
        }

        @Test
        @DisplayName("Should filter posts by title")
        void shouldFilterPostsByTitle() throws Exception {
            // Given
            Page<PostDTO.Res> postsPage = new PageImpl<>(List.of(testPostRes));
            when(adminPostService.getAllPost(any(PostDTO.Req.class), anyInt(), anyInt())).thenReturn(postsPage);

            // When/Then
            mockMvc.perform(get("/a/posts/list")
                            .param("title", "Test")
                            .param("page", "1")
                            .param("size", "10"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code", is(200)));

            verify(adminPostService).getAllPost(any(PostDTO.Req.class), anyInt(), anyInt());
        }

        @Test
        @DisplayName("Should filter posts by status")
        void shouldFilterPostsByStatus() throws Exception {
            // Given
            Page<PostDTO.Res> postsPage = new PageImpl<>(List.of(testPostRes));
            when(adminPostService.getAllPost(any(PostDTO.Req.class), anyInt(), anyInt())).thenReturn(postsPage);

            // When/Then
            mockMvc.perform(get("/a/posts/list")
                            .param("status", "PUBLISHED")
                            .param("page", "1")
                            .param("size", "10"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code", is(200)));

            verify(adminPostService).getAllPost(any(PostDTO.Req.class), anyInt(), anyInt());
        }
    }

    @Nested
    @DisplayName("GET /a/posts/drafts Tests")
    class GetAllDraftsTests {

        @Test
        @DisplayName("Should return all draft posts")
        void shouldReturnAllDraftPosts() throws Exception {
            // Given
            PostDTO.Res draftPost = new PostDTO.Res();
            draftPost.setId(1);
            draftPost.setTitle("Draft Post");
            draftPost.setStatus("DRAFT");

            Page<PostDTO.Res> draftsPage = new PageImpl<>(List.of(draftPost));
            when(adminPostService.getAllPost(any(PostDTO.Req.class), anyInt(), anyInt())).thenReturn(draftsPage);

            // When/Then
            mockMvc.perform(get("/a/posts/drafts")
                            .param("page", "1")
                            .param("size", "10"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code", is(200)))
                    .andExpect(jsonPath("$.message", containsString("Draft")));

            verify(adminPostService).getAllPost(any(PostDTO.Req.class), eq(0), eq(10));
        }
    }

    @Nested
    @DisplayName("GET /a/posts/{postId} Tests")
    class GetPostByIdTests {

        @Test
        @DisplayName("Should return post by id")
        void shouldReturnPostById() throws Exception {
            // Given
            when(adminPostService.detailPost(1)).thenReturn(testPostRes);

            // When/Then
            mockMvc.perform(get("/a/posts/1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code", is(200)))
                    .andExpect(jsonPath("$.data.title", is("Test Post")));

            verify(adminPostService).detailPost(1);
        }
    }

    @Nested
    @DisplayName("GET /a/posts/{postId}/edit Tests")
    class GetPostForEditTests {

        @Test
        @DisplayName("Should return post for editing")
        void shouldReturnPostForEditing() throws Exception {
            // Given
            when(adminPostService.detailPostForEdit(1)).thenReturn(testPostRes);

            // When/Then
            mockMvc.perform(get("/a/posts/1/edit"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code", is(200)))
                    .andExpect(jsonPath("$.message", containsString("editing")));

            verify(adminPostService).detailPostForEdit(1);
        }
    }

    @Nested
    @DisplayName("DELETE /a/posts/{postId} Tests")
    class DeletePostTests {

        @Test
        @DisplayName("Should delete post by id")
        void shouldDeletePostById() throws Exception {
            // Given
            doNothing().when(adminPostService).deletePost(1);

            // When/Then
            mockMvc.perform(delete("/a/posts/1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code", is(200)))
                    .andExpect(jsonPath("$.message", containsString("deleted")));

            verify(adminPostService).deletePost(1);
        }
    }
}

