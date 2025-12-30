package com.grummans.noyblog.controller.client;

import com.grummans.noyblog.dto.CategoryDTO;
import com.grummans.noyblog.services.client.ClientCategoryService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(
    controllers = ClientCategoryController.class,
    excludeAutoConfiguration = {SecurityAutoConfiguration.class},
    excludeFilters = @ComponentScan.Filter(type = FilterType.REGEX, pattern = "com.grummans.noyblog.configuration.*")
)
@DisplayName("ClientCategoryController Tests")
class ClientCategoryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ClientCategoryService clientCategoryService;

    @Test
    @DisplayName("GET /c/categories - Should return all categories")
    void shouldReturnAllCategories() throws Exception {
        // Given
        List<CategoryDTO.CategorySimpleDTO> categories = Arrays.asList(
                new CategoryDTO.CategorySimpleDTO(1, "Technology", "technology", 15),
                new CategoryDTO.CategorySimpleDTO(2, "Lifestyle", "lifestyle", 8)
        );
        when(clientCategoryService.getAllCategories()).thenReturn(categories);

        // When/Then
        mockMvc.perform(get("/c/categories"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code", is(200)))
                .andExpect(jsonPath("$.message", is("success")))
                .andExpect(jsonPath("$.data", hasSize(2)))
                .andExpect(jsonPath("$.data[0].name", is("Technology")))
                .andExpect(jsonPath("$.data[0].postCount", is(15)));

        verify(clientCategoryService).getAllCategories();
    }

    @Test
    @DisplayName("GET /c/categories - Should return empty list when no categories")
    void shouldReturnEmptyListWhenNoCategories() throws Exception {
        // Given
        when(clientCategoryService.getAllCategories()).thenReturn(List.of());

        // When/Then
        mockMvc.perform(get("/c/categories"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code", is(200)))
                .andExpect(jsonPath("$.data", hasSize(0)));

        verify(clientCategoryService).getAllCategories();
    }
}

