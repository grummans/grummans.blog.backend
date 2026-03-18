package com.grummans.noyblog.controller.admin;

import com.grummans.noyblog.configuration.ApiResponse;
import com.grummans.noyblog.model.PostAttachments;
import com.grummans.noyblog.services.system.FileService;

import lombok.RequiredArgsConstructor;

import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * Controller for managing post attachments (files displayed separately from content) These are different from inline
 * content files - they appear in a separate "Attachments" section
 */
@RestController
@RequestMapping("/a/posts/{postId}/attachments")
@RequiredArgsConstructor
public class PostAttachmentController {
    private final FileService fileService;

    /**
     * Upload attachment for a post Files will be stored in MinIO and metadata saved to post_attachment table
     *
     * @param postId  The post ID
     * @param file    The file to upload
     * @param altText Optional description (for images/documents)
     * @return PostAttachment with full metadata
     */
    @PostMapping(consumes = {"multipart/form-data"})
    public ApiResponse<PostAttachments> uploadAttachment(@PathVariable int postId,
                                                         @RequestPart("file") MultipartFile file, @RequestPart(value = "altText", required = false) String altText) {

        PostAttachments attachment = fileService.uploadPostAttachment(postId, file);

        if (altText != null && !altText.trim().isEmpty()) {
            attachment.setAltText(altText);
        }

        ApiResponse<PostAttachments> response = new ApiResponse<>();
        response.setCode(201);
        response.setMessage("Attachment uploaded successfully");
        response.setData(attachment);
        return response;
    }

    /**
     * Get all attachments for a post
     *
     * @param postId The post ID
     * @return List of attachments
     */
    @GetMapping
    public ApiResponse<List<PostAttachments>> getAttachments(@PathVariable int postId) {
        List<PostAttachments> attachments = fileService.getPostAttachments(postId);

        ApiResponse<List<PostAttachments>> response = new ApiResponse<>();
        response.setCode(200);
        response.setMessage("Attachments retrieved successfully");
        response.setData(attachments);
        return response;
    }

    /**
     * Delete a specific attachment
     *
     * @param postId       The post ID
     * @param attachmentId The attachment ID
     * @return Success response
     */
    @DeleteMapping("/{attachmentId}")
    public ApiResponse<Void> deleteAttachment(@PathVariable int postId, @PathVariable int attachmentId) {

        fileService.deleteAttachment(postId, attachmentId);

        ApiResponse<Void> response = new ApiResponse<>();
        response.setCode(200);
        response.setMessage("Attachment deleted successfully");
        return response;
    }
}

