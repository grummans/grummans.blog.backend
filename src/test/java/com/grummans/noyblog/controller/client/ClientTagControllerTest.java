package com.grummans.noyblog.controller.client;

import com.grummans.noyblog.dto.TagDTO;
import com.grummans.noyblog.services.client.ClientTagService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(
    controllers = ClientTagController.class,
    excludeAutoConfiguration = {SecurityAutoConfiguration.class},
    excludeFilters = @ComponentScan.Filter(type = FilterType.REGEX, pattern = "com.grummans.noyblog.configuration.*")
)
@DisplayName("ClientTagController Tests")
class ClientTagControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ClientTagService clientTagService;

    @Test
    @DisplayName("GET /c/tags - Should return all tags")
    void shouldReturnAllTags() throws Exception {
        // Given
        List<TagDTO.TagSimpleDTO> tags = Arrays.asList(
                new TagDTO.TagSimpleDTO(1, "Java", "java", 10),
                new TagDTO.TagSimpleDTO(2, "Spring", "spring", 5)
        );
        when(clientTagService.getAllTags()).thenReturn(tags);

        // When/Then
        mockMvc.perform(get("/c/tags"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code", is(200)))
                .andExpect(jsonPath("$.message", is("success")))
                .andExpect(jsonPath("$.data", hasSize(2)))
                .andExpect(jsonPath("$.data[0].name", is("Java")))
                .andExpect(jsonPath("$.data[0].postCount", is(10)));

        verify(clientTagService).getAllTags();
    }

    @Test
    @DisplayName("GET /c/tags - Should return empty list when no tags")
    void shouldReturnEmptyListWhenNoTags() throws Exception {
        // Given
        when(clientTagService.getAllTags()).thenReturn(List.of());

        // When/Then
        mockMvc.perform(get("/c/tags"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code", is(200)))
                .andExpect(jsonPath("$.data", hasSize(0)));

        verify(clientTagService).getAllTags();
    }
}

