package com.grummans.noyblog.controller.client;

import com.grummans.noyblog.configuration.ApiResponse;
import com.grummans.noyblog.dto.CategoryDTO;
import com.grummans.noyblog.services.client.ClientCategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/c/categories")
@RequiredArgsConstructor
public class ClientCategoryController {

    private final ClientCategoryService clientCategoryService;

    @GetMapping
    public ApiResponse<List<CategoryDTO.CategorySimpleDTO>> getAllCategories() {
        ApiResponse<List<CategoryDTO.CategorySimpleDTO>> response = new ApiResponse<>();
        response.setCode(200);
        response.setMessage("success");
        response.setData(clientCategoryService.getAllCategories());
        return response;
    }
}
