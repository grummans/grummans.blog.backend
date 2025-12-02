package com.grummans.noyblog.controller.admin;

import com.grummans.noyblog.configuration.ApiResponse;
import com.grummans.noyblog.dto.CategoryDTO;
import com.grummans.noyblog.services.admin.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/a/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    @GetMapping("/list")
    public ApiResponse<List<CategoryDTO.CategorySimpleDTO>> getAllCategories() {

        ApiResponse<List<CategoryDTO.CategorySimpleDTO>> response = new ApiResponse<>();
        response.setCode(200);
        response.setData(categoryService.getAllCategories());
        response.setMessage("success");
        return response;
    }

    @PostMapping("/create")
    public ApiResponse<CategoryDTO.CategorySimpleDTO> createCategory(CategoryDTO.Req req) {
        ApiResponse<CategoryDTO.CategorySimpleDTO> response = new ApiResponse<>();
        response.setCode(200);
        response.setData(categoryService.createCategory(req));
        response.setMessage("Category created successfully");
        return response;
    }

    @PutMapping("/update")
    public ApiResponse<CategoryDTO.CategorySimpleDTO> updateCategory(CategoryDTO.Req req) {
        ApiResponse<CategoryDTO.CategorySimpleDTO> response = new ApiResponse<>();
        response.setCode(200);
        response.setData(categoryService.updateCategory(req));
        response.setMessage("Category updated successfully");
        return response;
    }

    @DeleteMapping("/{categoryId}")
    public ApiResponse<Void> deleteCategory(@PathVariable int categoryId) {
        ApiResponse<Void> response = new ApiResponse<>();
        categoryService.deleteCategory(categoryId);
        response.setCode(200);
        response.setMessage("Category deleted successfully");
        return response;
    }

}
