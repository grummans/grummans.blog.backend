package com.grummans.noyblog.controller.admin;

import com.grummans.noyblog.configuration.ApiResponse;
import com.grummans.noyblog.configuration.PageResponse;
import com.grummans.noyblog.dto.PostDTO;
import com.grummans.noyblog.services.admin.AdminPostService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/a/posts")
@RequiredArgsConstructor
public class AdminPostController {

    private final AdminPostService postService;

    @GetMapping("/list")
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
     * Save post as draft (create new or update existing)
     * - If req.id is null/0: Create new draft
     * - If req.id exists: Update existing draft
     * - Allows incomplete data (category, tags, content can be null/empty)
     */
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
     * Publish a post (create new OR publish existing draft)
     * - If req.id is null/0: Create NEW post and publish directly
     * - If req.id exists: Update DRAFT and publish
     * - Requires complete post data (title, slug, category, tags, content)
     * - Validates all required fields before publishing
     */
    @PostMapping(value = "/publish", consumes = {"multipart/form-data"})
    public ApiResponse<PostDTO.SimplePostDTO> publishPost(
            @RequestPart("post") PostDTO.Req req,
            @RequestPart(value = "featuredImage", required = false) MultipartFile featuredImage) {
        ApiResponse<PostDTO.SimplePostDTO> response = new ApiResponse<>();
        boolean isUpdate = req.getId() != null && req.getId() > 0;

        PostDTO.SimplePostDTO publishedPost = postService.saveAndPublishPost(req, featuredImage);

        response.setCode(isUpdate ? 200 : 201);
        response.setMessage(isUpdate ? "Draft published successfully" : "Post created and published successfully");
        response.setData(publishedPost);
        return response;
    }

    @GetMapping("/{postId}")
    public ApiResponse<PostDTO.Res> getPostById(@PathVariable int postId) {
        ApiResponse<PostDTO.Res> response = new ApiResponse<>();
        response.setCode(200);
        response.setMessage("Post details retrieved successfully");
        response.setData(postService.detailPost(postId));
        return response;
    }

    @GetMapping("/{postId}/edit")
    public ApiResponse<PostDTO.Res> getPostForEdit(@PathVariable int postId) {
        ApiResponse<PostDTO.Res> response = new ApiResponse<>();
        response.setCode(200);
        response.setMessage("Post details for editing retrieved successfully");
        response.setData(postService.detailPostForEdit(postId));
        return response;
    }

    /**
     * Update an already published post
     * - Only works for posts with status = PUBLISHED
     * - Validates all required fields
     * - Updates content, tags, category, featured image
     * - Keeps publishedAt, updates updatedAt
     */
    @PutMapping(value = "/{postId}", consumes = {"multipart/form-data"})
    public ApiResponse<PostDTO.SimplePostDTO> updatePublishedPost(
            @PathVariable int postId,
            @RequestPart("post") PostDTO.Req req,
            @RequestPart(value = "featuredImage", required = false) MultipartFile featuredImage) {
        ApiResponse<PostDTO.SimplePostDTO> response = new ApiResponse<>();
        PostDTO.SimplePostDTO updatedPost = postService.updatePublishedPost(postId, req, featuredImage);

        response.setCode(200);
        response.setMessage("Post updated successfully");
        response.setData(updatedPost);
        return response;
    }

    @DeleteMapping("/{postId}")
    public ApiResponse<Void> deletePost(@PathVariable int postId) {
        ApiResponse<Void> response = new ApiResponse<>();
        postService.deletePost(postId);
        response.setCode(200);
        response.setMessage("Post deleted successfully");
        return response;
    }
}
