package com.grummans.noyblog.utils;

import com.grummans.noyblog.configuration.ApiResponse;
import io.minio.MinioClient;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

@RestController("/file")
@RequiredArgsConstructor
public class FileController {

    @Value("${minio.bucketName}")
    private String bucketName;

    private final MinioClient minioClient;

    @PatchMapping("/upload")
    public ApiResponse<String> uploadFile(@RequestParam MultipartFile file) throws IOException {

        String filename = UUID.randomUUID() + "_" + file.getOriginalFilename();


        return null;
    }
}
