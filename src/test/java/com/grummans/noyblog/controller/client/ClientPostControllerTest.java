package com.grummans.noyblog.controller.client;

import com.grummans.noyblog.dto.PostDTO;
import com.grummans.noyblog.services.client.ClientPostService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ClientPostController.class)
@DisplayName("ClientPostController Tests")
class ClientPostControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ClientPostService clientPostService;

    private PostDTO.PostForClientDTO testPostDTO;
    private PostDTO.Res testPostRes;

    @BeforeEach
    void setUp() {
        testPostDTO = new PostDTO.PostForClientDTO();
        testPostDTO.setId(1);
        testPostDTO.setTitle("Test Post");
        testPostDTO.setSlug("test-post");

        testPostRes = new PostDTO.Res();
        testPostRes.setId(1);
        testPostRes.setTitle("Test Post");
        testPostRes.setSlug("test-post");
    }

    @Test
    @DisplayName("GET /c/posts - Should return all posts")
    void shouldReturnAllPosts() throws Exception {
        // Given
        List<PostDTO.PostForClientDTO> posts = Arrays.asList(testPostDTO);
        when(clientPostService.getAllPosts()).thenReturn(posts);

        // When/Then
        mockMvc.perform(get("/c/posts"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code", is(200)))
                .andExpect(jsonPath("$.data", hasSize(1)))
                .andExpect(jsonPath("$.data[0].title", is("Test Post")));

        verify(clientPostService).getAllPosts();
    }

    @Test
    @DisplayName("GET /c/posts/featured - Should return featured posts")
    void shouldReturnFeaturedPosts() throws Exception {
        // Given
        List<PostDTO.PostForClientDTO> posts = Arrays.asList(testPostDTO);
        when(clientPostService.getFeaturedPosts()).thenReturn(posts);

        // When/Then
        mockMvc.perform(get("/c/posts/featured"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code", is(200)))
                .andExpect(jsonPath("$.data", hasSize(1)));

        verify(clientPostService).getFeaturedPosts();
    }

    @Test
    @DisplayName("GET /c/posts/{postId} - Should return post by id")
    void shouldReturnPostById() throws Exception {
        // Given
        when(clientPostService.getDetailPost(1)).thenReturn(testPostRes);

        // When/Then
        mockMvc.perform(get("/c/posts/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code", is(200)))
                .andExpect(jsonPath("$.data.title", is("Test Post")));

        verify(clientPostService).getDetailPost(1);
    }

    @Test
    @DisplayName("GET /c/posts/slug/{slug} - Should return post by slug")
    void shouldReturnPostBySlug() throws Exception {
        // Given
        when(clientPostService.getDetailPostBySlug("test-post")).thenReturn(testPostRes);

        // When/Then
        mockMvc.perform(get("/c/posts/slug/test-post"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code", is(200)))
                .andExpect(jsonPath("$.data.slug", is("test-post")));

        verify(clientPostService).getDetailPostBySlug("test-post");
    }

    @Test
    @DisplayName("GET /c/posts - Should return empty list when no posts")
    void shouldReturnEmptyListWhenNoPosts() throws Exception {
        // Given
        when(clientPostService.getAllPosts()).thenReturn(List.of());

        // When/Then
        mockMvc.perform(get("/c/posts"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code", is(200)))
                .andExpect(jsonPath("$.data", hasSize(0)));

        verify(clientPostService).getAllPosts();
    }
}

