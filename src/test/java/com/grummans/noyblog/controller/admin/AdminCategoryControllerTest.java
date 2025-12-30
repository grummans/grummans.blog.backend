package com.grummans.noyblog.controller.admin;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.grummans.noyblog.dto.CategoryDTO;
import com.grummans.noyblog.services.admin.AdminCategoryService;
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
    controllers = AdminCategoryController.class,
    excludeAutoConfiguration = {SecurityAutoConfiguration.class},
    excludeFilters = @ComponentScan.Filter(type = FilterType.REGEX, pattern = "com.grummans.noyblog.configuration.*")
)
@DisplayName("AdminCategoryController Tests")
class AdminCategoryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AdminCategoryService adminCategoryService;

    @Autowired
    private ObjectMapper objectMapper;

    private CategoryDTO.CategorySimpleDTO testCategoryDTO;
    private CategoryDTO.Res testCategoryRes;
    private CategoryDTO.Req testCategoryReq;

    @BeforeEach
    void setUp() {
        testCategoryDTO = new CategoryDTO.CategorySimpleDTO(1, "Technology", "technology", 15);
        testCategoryRes = new CategoryDTO.Res("Technology", "technology", "Tech articles", "#3498db");
        testCategoryReq = new CategoryDTO.Req("Technology", "technology", "Tech articles", "#3498db");
    }

    @Test
    @DisplayName("GET /a/categories - Should return all categories")
    void shouldReturnAllCategories() throws Exception {
        // Given
        List<CategoryDTO.CategorySimpleDTO> categories = Arrays.asList(
                new CategoryDTO.CategorySimpleDTO(1, "Tech", "tech", 10),
                new CategoryDTO.CategorySimpleDTO(2, "Life", "life", 5)
        );
        when(adminCategoryService.getAllCategories()).thenReturn(categories);

        // When/Then
        mockMvc.perform(get("/a/categories"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code", is(200)))
                .andExpect(jsonPath("$.message", is("success")))
                .andExpect(jsonPath("$.data", hasSize(2)))
                .andExpect(jsonPath("$.data[0].name", is("Tech")));

        verify(adminCategoryService).getAllCategories();
    }

    @Test
    @DisplayName("POST /a/categories/create - Should create category")
    void shouldCreateCategory() throws Exception {
        // Given
        when(adminCategoryService.createCategory(any(CategoryDTO.Req.class))).thenReturn(testCategoryDTO);

        // When/Then
        mockMvc.perform(post("/a/categories/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testCategoryReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code", is(200)))
                .andExpect(jsonPath("$.message", is("Category created successfully")))
                .andExpect(jsonPath("$.data.name", is("Technology")));

        verify(adminCategoryService).createCategory(any(CategoryDTO.Req.class));
    }

    @Test
    @DisplayName("GET /a/categories/{categoryId} - Should return category by id")
    void shouldReturnCategoryById() throws Exception {
        // Given
        when(adminCategoryService.getDetailCategory(1)).thenReturn(testCategoryRes);

        // When/Then
        mockMvc.perform(get("/a/categories/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code", is(200)))
                .andExpect(jsonPath("$.data.name", is("Technology")))
                .andExpect(jsonPath("$.data.description", is("Tech articles")));

        verify(adminCategoryService).getDetailCategory(1);
    }

    @Test
    @DisplayName("PUT /a/categories/{categoryId}/update - Should update category")
    void shouldUpdateCategory() throws Exception {
        // Given
        CategoryDTO.CategorySimpleDTO updatedCategory = new CategoryDTO.CategorySimpleDTO(1, "Updated Tech", "updated-tech", 15);
        when(adminCategoryService.updateCategory(eq(1), any(CategoryDTO.Req.class))).thenReturn(updatedCategory);

        // When/Then
        mockMvc.perform(put("/a/categories/1/update")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testCategoryReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code", is(200)))
                .andExpect(jsonPath("$.message", is("Category updated successfully")))
                .andExpect(jsonPath("$.data.name", is("Updated Tech")));

        verify(adminCategoryService).updateCategory(eq(1), any(CategoryDTO.Req.class));
    }

    @Test
    @DisplayName("DELETE /a/categories/{categoryId} - Should delete category")
    void shouldDeleteCategory() throws Exception {
        // Given
        doNothing().when(adminCategoryService).deleteCategory(1);

        // When/Then
        mockMvc.perform(delete("/a/categories/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code", is(200)))
                .andExpect(jsonPath("$.message", is("Category deleted successfully")));

        verify(adminCategoryService).deleteCategory(1);
    }
}

