package com.grummans.noyblog.controller.admin;

import com.grummans.noyblog.configuration.ApiResponse;
import com.grummans.noyblog.dto.CategoryDTO;
import com.grummans.noyblog.services.admin.AdminCategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/a/categories")
@RequiredArgsConstructor
public class AdminCategoryController {

    private final AdminCategoryService categoryService;

    @GetMapping
    public ApiResponse<List<CategoryDTO.CategorySimpleDTO>> getAllCategories() {

        ApiResponse<List<CategoryDTO.CategorySimpleDTO>> response = new ApiResponse<>();
        response.setCode(200);
        response.setData(categoryService.getAllCategories());
        response.setMessage("success");
        return response;
    }

    @PostMapping("/create")
    public ApiResponse<CategoryDTO.CategorySimpleDTO> createCategory(@RequestBody CategoryDTO.Req req) {
        ApiResponse<CategoryDTO.CategorySimpleDTO> response = new ApiResponse<>();
        response.setCode(200);
        response.setData(categoryService.createCategory(req));
        response.setMessage("Category created successfully");
        return response;
    }

    @GetMapping("/{categoryId}")
    public ApiResponse<CategoryDTO.Res> getCategory(@PathVariable Integer categoryId) {
        ApiResponse<CategoryDTO.Res> response = new ApiResponse<>();
        response.setCode(200);
        response.setData(categoryService.getDetailCategory(categoryId));
        response.setMessage("success");
        return response;
    }

    @PutMapping("/{categoryId}/update")
    public ApiResponse<CategoryDTO.CategorySimpleDTO> updateCategory(@PathVariable int categoryId, @RequestBody CategoryDTO.Req req) {
        ApiResponse<CategoryDTO.CategorySimpleDTO> response = new ApiResponse<>();
        response.setCode(200);
        response.setData(categoryService.updateCategory(categoryId, req));
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
