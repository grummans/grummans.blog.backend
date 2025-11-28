package com.grummans.noyblog.services.system;

import com.grummans.noyblog.exceptions.FileUploadException;
import com.grummans.noyblog.model.PostAttachment;
import com.grummans.noyblog.repository.PostAttachmentRepository;
import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectArgs;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FileService {

    @Value("${minio.endpoint}")
    private String minioEndpoint;

    private final MinioClient minioClient;

    private final PostAttachmentRepository postAttachmentRepository;

    private static final List<String> ALLOWED_IMAGE_EXTENSIONS =
            Arrays.asList(".jpg", ".jpeg", ".png", ".gif", ".webp", ".svg", ".bmp", ".ico");

    private static final List<String> ALLOWED_DOCUMENT_EXTENSIONS =
            Arrays.asList(".pdf", ".doc", ".docx", ".txt", ".md", ".xls", ".xlsx", ".ppt", ".pptx", ".odt", ".rtf");

    private static final List<String> ALLOWED_ARCHIVE_EXTENSIONS =
            Arrays.asList(".zip", ".rar", ".7z", ".tar", ".gz", ".bz2", ".xz");

    private static final List<String> ALLOWED_CODE_EXTENSIONS =
            Arrays.asList(".json", ".xml", ".yaml", ".yml", ".sql", ".csv");

    private static final long MAX_IMAGE_SIZE = 10 * 1024 * 1024; // 10 MB
    private static final long MAX_DOCUMENT_SIZE = 20 * 1024 * 1024; // 20 MB
    private static final long MAX_ARCHIVE_SIZE = 100 * 1024 * 1024; // 100 MB
    private static final long MAX_CODE_SIZE = 5 * 1024 * 1024; // 5 MB

    /**
     * Upload featured image for a post (only stores the file, doesn't save to DB)
     *
     * @param postId The post ID
     * @param file   The image file
     * @return The public URL of the uploaded image
     */
    public String uploadFeaturedImage(int postId, MultipartFile file) {
        validateFile(file);

        String fileUUID = UUID.randomUUID().toString();
        String extension = getFileExtension(file.getOriginalFilename());
        String storedFileName = fileUUID + extension;

        // Minio path (without bucket name): {postId}/featured/{uuid}.ext
        String storagePath = String.format("%d/featured/%s", postId, storedFileName);

        try {
            uploadToMinio(storagePath, file, "posts");
            // URL format: https://minioconsole.grummans.me/posts/{postId}/featured/{uuid}.jpg
            return generateFileUrl("posts/" + storagePath);
        } catch (Exception e) {
            throw new FileUploadException("Failed to upload featured image: " + e.getMessage());
        }
    }

    /**
     * Upload attachment for a post (saves to DB)
     *
     * @param postId The post ID
     * @param file   The attachment file
     * @return PostAttachment entity
     */
    public PostAttachment uploadPostAttachment(int postId, MultipartFile file) {
        validateFile(file);

        String fileUUID = UUID.randomUUID().toString();
        String extension = getFileExtension(file.getOriginalFilename());
        String storedFileName = fileUUID + extension;

        //Minio path (without bucket name): {postId}/attachments/{uuid}.ext
        String storagePath = String.format("%d/attachments/%s", postId, storedFileName);

        try {
            uploadToMinio(storagePath, file, "posts");

            PostAttachment postAttachment = new PostAttachment();
            postAttachment.setPostId(postId);
            postAttachment.setOriginalFileName(file.getOriginalFilename());
            postAttachment.setStoredFileName(storedFileName);
            postAttachment.setFileType(determineFileType(file.getContentType()));
            postAttachment.setMimeType(file.getContentType());
            postAttachment.setFileSize(file.getSize());
            postAttachment.setStoragePath(storagePath);
            return postAttachmentRepository.save(postAttachment);
        } catch (Exception e) {
            throw new FileUploadException("Failed to upload attachment: " + e.getMessage());
        }
    }

    /**
     * Upload user avatar (only stores the file, doesn't save to DB)
     *
     * @param userId The user ID
     * @param file   The avatar image file
     * @return The public URL of the uploaded avatar
     */
    public String uploadUserAvatar(int userId, MultipartFile file) {
        validateFile(file);

        String fileUUID = UUID.randomUUID().toString();
        String extension = getFileExtension(file.getOriginalFilename());
        String storedFileName = fileUUID + extension;

        // Minio path (without bucket name): {userId}/avatar/{uuid}.ext
        String storagePath = String.format("%d/avatar/%s", userId, storedFileName);

        try {
            uploadToMinio(storagePath, file, "users");
            return generateFileUrl("users/" + storagePath);
        } catch (Exception e) {
            throw new FileUploadException("Failed to upload avatar: " + e.getMessage());
        }
    }

    /**
     * Upload inline file/image for TipTap Editor content (when post doesn't exist yet)
     * Files are stored in temp folder first, then moved to post folder after post is created
     * Supports: images, documents, archives (zip/rar), code files
     *
     * @param file The file to upload
     * @return The public URL of the uploaded file
     */
    public String uploadContentFile(MultipartFile file) {
        validateFile(file);

        String fileUUID = UUID.randomUUID().toString();
        String extension = getFileExtension(file.getOriginalFilename());
        String storedFileName = fileUUID + extension;

        // Minio path (without bucket name): temp/content-files/{uuid}.ext
        String storagePath = String.format("temp/content-files/%s", storedFileName);

        try {
            uploadToMinio(storagePath, file, "posts");
            return generateFileUrl("posts/" + storagePath);
        } catch (Exception e) {
            throw new FileUploadException("Failed to upload content file: " + e.getMessage());
        }
    }

    /**
     * @deprecated Use uploadContentFile() instead for better flexibility
     */
    @Deprecated
    public String uploadContentImage(MultipartFile file) {
        return uploadContentFile(file);
    }

    /**
     * Move content files (images, documents, archives, etc.) from temp folder to post folder after post is created
     * This method should be called after post is saved
     *
     * @param postId   The post ID
     * @param fileUrls List of file URLs in the content
     */
    public void moveContentFilesToPost(int postId, List<String> fileUrls) {
        if (fileUrls == null || fileUrls.isEmpty()) {
            return;
        }

        for (String fileUrl : fileUrls) {
            try {
                // Extract storage path from URL
                // URL format: https://minioconsole.grummans.me/posts/temp/content-files/uuid.jpg
                String storagePath = fileUrl.replace(minioEndpoint + "/", "");

                // Only process temp files
                if (!storagePath.startsWith("posts/temp/content-files/") && !storagePath.startsWith("posts/temp/content-images/")) {
                    continue;
                }

                // Extract filename
                String fileName = storagePath.substring(storagePath.lastIndexOf("/") + 1);

                // Remove bucket name prefix - convert "posts/temp/..." to just "temp/..."
                String objectPath = storagePath.substring("posts/".length());

                // New path (without bucket prefix): {postId}/content/{filename}
                String newObjectPath = String.format("%d/content/%s", postId, fileName);

                System.out.println("[MinIO] Moving file:");
                System.out.println("[MinIO] From: " + objectPath);
                System.out.println("[MinIO] To: " + newObjectPath);

                // Copy object to new location
                minioClient.copyObject(
                        io.minio.CopyObjectArgs.builder()
                                .bucket("posts")
                                .object(newObjectPath)
                                .source(
                                        io.minio.CopySource.builder()
                                                .bucket("posts")
                                                .object(objectPath)
                                                .build()
                                )
                                .build()
                );

                // Delete old object
                minioClient.removeObject(
                        RemoveObjectArgs.builder()
                                .bucket("posts")
                                .object(objectPath)
                                .build()
                );

                System.out.println("[MinIO] File moved successfully");

            } catch (Exception e) {
                // Log error but don't throw - allow post creation to succeed
                System.err.println("Failed to move content file: " + e.getMessage());
            }
        }
    }

    /**
     * @deprecated Use moveContentFilesToPost() instead
     */
    @Deprecated
    public void moveContentImagesToPost(int postId, List<String> imageUrls) {
        moveContentFilesToPost(postId, imageUrls);
    }

    public void deleteFile(int attachmentId) {

        PostAttachment postAttachment = postAttachmentRepository.findById(attachmentId)
                .orElseThrow(() -> new FileUploadException("Attachment not found"));

        try {
            minioClient.removeObject(
                    RemoveObjectArgs.builder()
                            .bucket("posts")
                            .object(postAttachment.getStoragePath())
                            .build()
            );
            postAttachmentRepository.deleteById(attachmentId);
        } catch (Exception e) {
            throw new FileUploadException("Failed to delete file: " + e.getMessage());
        }
    }

    // Validate the uploaded file
    private void validateFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new FileUploadException("File is empty");
        }

        String extension = getFileExtension(file.getOriginalFilename());

        // Determine file category based on extension
        String fileCategory = determineFileCategory(extension);

        // Get max file size based on category
        long maxFileSize;
        List<String> allowedExtensions;

        switch (fileCategory) {
            case "image":
                maxFileSize = MAX_IMAGE_SIZE;
                allowedExtensions = ALLOWED_IMAGE_EXTENSIONS;
                break;
            case "document":
                maxFileSize = MAX_DOCUMENT_SIZE;
                allowedExtensions = ALLOWED_DOCUMENT_EXTENSIONS;
                break;
            case "archive":
                maxFileSize = MAX_ARCHIVE_SIZE;
                allowedExtensions = ALLOWED_ARCHIVE_EXTENSIONS;
                break;
            case "code":
                maxFileSize = MAX_CODE_SIZE;
                allowedExtensions = ALLOWED_CODE_EXTENSIONS;
                break;
            default:
                throw new FileUploadException("File type not supported: " + extension);
        }

        // Validate file size
        if (file.getSize() > maxFileSize) {
            throw new FileUploadException(
                    String.format("File size exceeds maximum allowed size of %d MB for %s files",
                            maxFileSize / (1024 * 1024), fileCategory)
            );
        }

        // Validate extension
        if (!allowedExtensions.contains(extension)) {
            throw new FileUploadException(
                    String.format("File extension %s not allowed. Allowed %s types: %s",
                            extension, fileCategory, allowedExtensions)
            );
        }
    }

    /**
     * Determine file category based on extension
     */
    private String determineFileCategory(String extension) {
        if (ALLOWED_IMAGE_EXTENSIONS.contains(extension)) {
            return "image";
        } else if (ALLOWED_DOCUMENT_EXTENSIONS.contains(extension)) {
            return "document";
        } else if (ALLOWED_ARCHIVE_EXTENSIONS.contains(extension)) {
            return "archive";
        } else if (ALLOWED_CODE_EXTENSIONS.contains(extension)) {
            return "code";
        }
        return "unknown";
    }

    /**
     * Helper: Determine file type based on mime type
     */
    private String determineFileType(String contentType) {
        if (contentType == null) {
            return "unknown";
        } else if (contentType.startsWith("image/")) {
            return "image";
        } else if (contentType.startsWith("video/")) {
            return "video";
        } else if (contentType.startsWith("audio/")) {
            return "audio";
        } else if (contentType.equals("application/pdf")) {
            return "document";
        } else {
            return "other";
        }
    }

    private String getFileExtension(String filename) {
        if (filename == null || filename.isEmpty()) {
            throw new FileUploadException("Filename is empty");
        }

        int lastDotIndex = filename.lastIndexOf('.');
        if (lastDotIndex == -1) {
            throw new FileUploadException("File has no extension");
        }

        return filename.substring(lastDotIndex).toLowerCase();
    }

    /**
     * Ensure bucket exists on MinIO server, create if not exists
     *
     * @param bucketName The bucket name to check/create
     */
    private void ensureBucketExists(String bucketName) throws Exception {
        System.out.println("[MinIO] Checking bucket: " + bucketName);
        boolean exists = minioClient.bucketExists(
                BucketExistsArgs.builder()
                        .bucket(bucketName)
                        .build()
        );

        if (!exists) {
            System.out.println("[MinIO] Bucket not found, creating: " + bucketName);
            minioClient.makeBucket(
                    MakeBucketArgs.builder()
                            .bucket(bucketName)
                            .build()
            );
            System.out.println("[MinIO] Successfully created bucket: " + bucketName);
        } else {
            System.out.println("[MinIO] Bucket already exists: " + bucketName);
        }
    }

    private void uploadToMinio(String storagePath, MultipartFile file, String bucketName) throws Exception {
        System.out.println("[MinIO] === Upload Start ===");
        System.out.println("[MinIO] Bucket: " + bucketName);
        System.out.println("[MinIO] Storage Path: " + storagePath);
        System.out.println("[MinIO] File Name: " + file.getOriginalFilename());

        // Ensure bucket exists before upload
        ensureBucketExists(bucketName);

        try (InputStream inputStream = file.getInputStream()) {
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(bucketName)
                            .object(storagePath)
                            .stream(inputStream, file.getSize(), -1)
                            .contentType(file.getContentType())
                            .build()
            );
            System.out.println("[MinIO] Upload successful");
        }
    }

    /**
     * Generate a public URL for accessing the file stored in MinIO.
     *
     * @param storagePath The storage path of the file in MinIO
     * @return Public URL as a String
     */
    public String generateFileUrl(String storagePath) {
        return String.format("%s/%s", minioEndpoint, storagePath);
    }

    /**
     * Extract all file URLs from HTML content (from TipTap Editor)
     * Looks for:
     * - <img> tags with src attribute
     * - <a> tags with href attribute pointing to files
     *
     * @param htmlContent The HTML content to parse
     * @return List of file URLs found in the content
     */
    public List<String> extractFileUrlsFromContent(String htmlContent) {
        if (htmlContent == null || htmlContent.isEmpty()) {
            return List.of();
        }

        List<String> fileUrls = new java.util.ArrayList<>();

        // Pattern to match img src: <img src="url" ...>
        java.util.regex.Pattern imgPattern = java.util.regex.Pattern.compile(
                "<img[^>]+src=\"([^\"]+)\"",
                java.util.regex.Pattern.CASE_INSENSITIVE
        );
        java.util.regex.Matcher imgMatcher = imgPattern.matcher(htmlContent);
        while (imgMatcher.find()) {
            String url = imgMatcher.group(1);
            // Only add URLs from our MinIO endpoint
            if (url.startsWith(minioEndpoint)) {
                fileUrls.add(url);
            }
        }

        // Pattern to match <a> tags with file links: <a href="url" ...>
        // Only match if href contains file extensions
        java.util.regex.Pattern linkPattern = java.util.regex.Pattern.compile(
                "<a[^>]+href=\"([^\"]+\\.(pdf|doc|docx|txt|md|xls|xlsx|ppt|pptx|zip|rar|7z|tar|gz|json|xml|yaml|yml|sql|csv))\"",
                java.util.regex.Pattern.CASE_INSENSITIVE
        );
        java.util.regex.Matcher linkMatcher = linkPattern.matcher(htmlContent);
        while (linkMatcher.find()) {
            String url = linkMatcher.group(1);
            // Only add URLs from our MinIO endpoint
            if (url.startsWith(minioEndpoint)) {
                fileUrls.add(url);
            }
        }

        return fileUrls;
    }


}
