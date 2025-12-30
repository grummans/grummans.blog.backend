package com.grummans.noyblog.controller.admin;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.grummans.noyblog.dto.TagDTO;
import com.grummans.noyblog.services.admin.AdminTagService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(
    controllers = AdminTagController.class,
    excludeAutoConfiguration = {SecurityAutoConfiguration.class},
    excludeFilters = @ComponentScan.Filter(type = FilterType.REGEX, pattern = "com.grummans.noyblog.configuration.*")
)
@DisplayName("AdminTagController Tests")
class AdminTagControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AdminTagService adminTagService;

    @Autowired
    private ObjectMapper objectMapper;

    private TagDTO.TagSimpleDTO testTagDTO;
    private TagDTO.Req testTagReq;

    @BeforeEach
    void setUp() {
        testTagDTO = new TagDTO.TagSimpleDTO(1, "Java", "java", 10);
        testTagReq = new TagDTO.Req("Java", "java");
    }

    @Test
    @DisplayName("GET /a/tags - Should return all tags")
    void shouldReturnAllTags() throws Exception {
        // Given
        List<TagDTO.TagSimpleDTO> tags = Arrays.asList(
                new TagDTO.TagSimpleDTO(1, "Java", "java", 10),
                new TagDTO.TagSimpleDTO(2, "Spring", "spring", 5)
        );
        when(adminTagService.getAllTags()).thenReturn(tags);

        // When/Then
        mockMvc.perform(get("/a/tags"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code", is(200)))
                .andExpect(jsonPath("$.message", is("success")))
                .andExpect(jsonPath("$.data", hasSize(2)))
                .andExpect(jsonPath("$.data[0].name", is("Java")))
                .andExpect(jsonPath("$.data[1].name", is("Spring")));

        verify(adminTagService).getAllTags();
    }

    @Test
    @DisplayName("POST /a/tags/create - Should create tag")
    void shouldCreateTag() throws Exception {
        // Given
        when(adminTagService.createTag(any(TagDTO.Req.class))).thenReturn(testTagDTO);

        // When/Then
        mockMvc.perform(post("/a/tags/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testTagReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code", is(200)))
                .andExpect(jsonPath("$.message", is("Tag created successfully")))
                .andExpect(jsonPath("$.data.name", is("Java")));

        verify(adminTagService).createTag(any(TagDTO.Req.class));
    }

    @Test
    @DisplayName("GET /a/tags/{tagId} - Should return tag by id")
    void shouldReturnTagById() throws Exception {
        // Given
        when(adminTagService.getDetailTag(1)).thenReturn(testTagDTO);

        // When/Then
        mockMvc.perform(get("/a/tags/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code", is(200)))
                .andExpect(jsonPath("$.data.id", is(1)))
                .andExpect(jsonPath("$.data.name", is("Java")));

        verify(adminTagService).getDetailTag(1);
    }

    @Test
    @DisplayName("PUT /a/tags/{tagId}/update - Should update tag")
    void shouldUpdateTag() throws Exception {
        // Given
        TagDTO.TagSimpleDTO updatedTag = new TagDTO.TagSimpleDTO(1, "Updated Java", "updated-java", 10);
        when(adminTagService.updateTag(eq(1), any(TagDTO.Req.class))).thenReturn(updatedTag);

        // When/Then
        mockMvc.perform(put("/a/tags/1/update")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testTagReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code", is(200)))
                .andExpect(jsonPath("$.message", is("Tag updated successfully")))
                .andExpect(jsonPath("$.data.name", is("Updated Java")));

        verify(adminTagService).updateTag(eq(1), any(TagDTO.Req.class));
    }

    @Test
    @DisplayName("DELETE /a/tags/{tagId} - Should delete tag")
    void shouldDeleteTag() throws Exception {
        // Given
        doNothing().when(adminTagService).deleteTag(1);

        // When/Then
        mockMvc.perform(delete("/a/tags/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code", is(200)))
                .andExpect(jsonPath("$.message", is("Tag deleted successfully")));

        verify(adminTagService).deleteTag(1);
    }
}

