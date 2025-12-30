package com.grummans.noyblog.controller.system.file;

import com.grummans.noyblog.configuration.ApiResponse;
import com.grummans.noyblog.services.system.FileService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/file")
@RequiredArgsConstructor
public class FileController {

    private final FileService fileService;

    /**
     * Upload file for TipTap Editor content (images, documents, archives, etc.)
     * This endpoint is called when user drags/pastes file into the editor
     * Supports: .jpg, .png, .gif, .pdf, .zip, .rar, .doc, .xls, etc.
     */
    @PostMapping(value = "/upload-content-file", consumes = {"multipart/form-data"})
    public ApiResponse<String> uploadContentFile(
            @RequestParam("file") MultipartFile file) {
        ApiResponse<String> response = new ApiResponse<>();
        response.setCode(200);
        response.setData(fileService.uploadContentFile(file));
        response.setMessage("File uploaded successfully");
        return response;
    }

    /**
     * Upload avatar for user
     */
    @PostMapping(value = "/upload-avatar", consumes = {"multipart/form-data"})
    public ApiResponse<String> uploadAvatar(
            @RequestParam("userId") int userId,
            @RequestParam("file") MultipartFile file) {
        ApiResponse<String> response = new ApiResponse<>();
        response.setCode(200);
        response.setData(fileService.uploadUserAvatar(userId, file));
        response.setMessage("Avatar uploaded successfully");
        return response;
    }
}
