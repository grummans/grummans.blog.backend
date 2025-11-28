package com.grummans.noyblog.controller.admin;

import com.grummans.noyblog.configuration.ApiResponse;
import com.grummans.noyblog.configuration.PageResponse;
import com.grummans.noyblog.dto.PostDTO;
import com.grummans.noyblog.services.admin.PostService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/a/posts")
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;

    @GetMapping("/list")
    @CrossOrigin
    public ApiResponse<PageResponse<PostDTO.Res>> getAllPosts(
            @RequestParam(required = false) String title,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        ApiResponse<PageResponse<PostDTO.Res>> response = new ApiResponse<>();
        int pageNumber = page > 0 ? page - 1 : 0;

        PostDTO.Req req = new PostDTO.Req();
        req.setTitle(title);

        Page<PostDTO.Res> postsPage = postService.getAllPost(req, pageNumber, size);

        response.setCode(200);
        response.setData(new PageResponse<>(postsPage));
        return response;
    }

    @CrossOrigin
    @PostMapping(value = "/create", consumes = {"multipart/form-data"})
    public ApiResponse<PostDTO.SimplePostDTO> createPost(
            @RequestPart("post") PostDTO.Req req,
            @RequestPart(value = "featuredImage", required = false) org.springframework.web.multipart.MultipartFile featuredImage) {
        ApiResponse<PostDTO.SimplePostDTO> response = new ApiResponse<>();
        response.setCode(201);
        response.setData(postService.createPost(req, featuredImage));
        return response;
    }

    @CrossOrigin
    @GetMapping("/{postId}")
    public ApiResponse<PostDTO.Res> getPostById(@PathVariable int postId) {
        ApiResponse<PostDTO.Res> response = new ApiResponse<>();
        response.setCode(200);
        response.setMessage("Post details retrieved successfully");
        response.setData(postService.detailPost(postId));
        return response;
    }

    @CrossOrigin
    @GetMapping("/{postId}/edit")
    public ApiResponse<PostDTO.Res> getPostForEdit(@PathVariable int postId) {
        ApiResponse<PostDTO.Res> response = new ApiResponse<>();
        response.setCode(200);
        response.setMessage("Post details for editing retrieved successfully");
        response.setData(postService.detailPostForEdit(postId));
        return response;
    }
}
