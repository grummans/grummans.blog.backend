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
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        ApiResponse<PageResponse<PostDTO.Res>> response = new ApiResponse<>();
        int pageNumber = page > 0 ? page - 1 : 0;

        PostDTO.Req req = new PostDTO.Req();
        req.setTitle(title);
        req.setStatus(status);

        Page<PostDTO.Res> postsPage = postService.getAllPost(req, pageNumber, size);

        response.setCode(200);
        response.setData(new PageResponse<>(postsPage));
        return response;
    }

    /**
     * Get all draft posts
     */
    @GetMapping("/drafts")
    @CrossOrigin
    public ApiResponse<PageResponse<PostDTO.Res>> getAllDrafts(
            @RequestParam(required = false) String title,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        ApiResponse<PageResponse<PostDTO.Res>> response = new ApiResponse<>();
        int pageNumber = page > 0 ? page - 1 : 0;

        PostDTO.Req req = new PostDTO.Req();
        req.setTitle(title);
        req.setStatus("DRAFT");

        Page<PostDTO.Res> postsPage = postService.getAllPost(req, pageNumber, size);

        response.setCode(200);
        response.setMessage("Draft posts retrieved successfully");
        response.setData(new PageResponse<>(postsPage));
        return response;
    }

    /**
     * Create and publish a post directly
     */
    @CrossOrigin
    @PostMapping(value = "/create", consumes = {"multipart/form-data"})
    public ApiResponse<PostDTO.SimplePostDTO> createPost(
            @RequestPart("post") PostDTO.Req req,
            @RequestPart(value = "featuredImage", required = false) MultipartFile featuredImage) {
        ApiResponse<PostDTO.SimplePostDTO> response = new ApiResponse<>();
        response.setCode(201);
        response.setMessage("Post created and published successfully");
        response.setData(postService.createPost(req, featuredImage));
        return response;
    }

    /**
     * Save post as draft (create new or update existing)
     * - If req.id is null/0: Create new draft
     * - If req.id exists: Update existing draft
     */
    @CrossOrigin
    @PostMapping(value = "/save-draft", consumes = {"multipart/form-data"})
    public ApiResponse<PostDTO.SimplePostDTO> saveDraft(
            @RequestPart("post") PostDTO.Req req,
            @RequestPart(value = "featuredImage", required = false) MultipartFile featuredImage) {
        ApiResponse<PostDTO.SimplePostDTO> response = new ApiResponse<>();
        PostDTO.SimplePostDTO savedDraft = postService.saveDraftPost(req, featuredImage);

        boolean isUpdate = req.getId() != null && req.getId() > 0;
        response.setCode(isUpdate ? 200 : 201);
        response.setMessage(isUpdate ? "Draft updated successfully" : "Draft created successfully");
        response.setData(savedDraft);
        return response;
    }

    /**
     * Publish a draft post
     */
    @CrossOrigin
    @PostMapping("/{postId}/publish")
    public ApiResponse<PostDTO.SimplePostDTO> publishDraft(@PathVariable int postId) {
        ApiResponse<PostDTO.SimplePostDTO> response = new ApiResponse<>();
        response.setCode(200);
        response.setMessage("Draft published successfully");
        response.setData(postService.publishDraft(postId));
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

//    @CrossOrigin
//    @PostMapping(value = "/save")
//    public ApiResponse<PostDTO.SimplePostDTO> savePost() {
//
//    }

    @CrossOrigin
    @DeleteMapping("/{postId}")
    public ApiResponse<Void> deletePost(@PathVariable int postId) {
        ApiResponse<Void> response = new ApiResponse<>();
        postService.deletePost(postId);
        response.setCode(200);
        response.setMessage("Post deleted successfully");
        return response;
    }
}
