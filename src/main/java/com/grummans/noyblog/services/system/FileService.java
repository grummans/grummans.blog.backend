package com.grummans.noyblog.services.system;

import com.grummans.noyblog.exceptions.FileUploadException;
import com.grummans.noyblog.model.PostAttachments;
import com.grummans.noyblog.repository.PostAttachmentRepository;

import io.minio.*;
import io.minio.messages.Item;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
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
            Arrays.asList(".pdf", ".doc", ".docx", ".txt", ".md", ".xls", ".xlsx", ".ppt", ".pptx",
                    ".odt", ".rtf");

    private static final List<String> ALLOWED_ARCHIVE_EXTENSIONS =
            Arrays.asList(".zip", ".rar", ".7z", ".tar", ".gz", ".bz2", ".xz");

    private static final List<String> ALLOWED_CODE_EXTENSIONS =
            Arrays.asList(".json", ".xml", ".yaml", ".yml", ".sql", ".csv");

    private static final long MAX_IMAGE_SIZE = 10L * 1024 * 1024; // 10 MB

    private static final long MAX_DOCUMENT_SIZE = 20L * 1024 * 1024; // 20 MB

    private static final long MAX_ARCHIVE_SIZE = 100L * 1024 * 1024; // 100 MB

    private static final long MAX_CODE_SIZE = 5L * 1024 * 1024; // 5 MB

    // Bucket names
    private static final String BUCKET_POSTS = "posts";
    private static final String BUCKET_USERS = "users";
    private static final String BUCKET_POSTS_PREFIX = BUCKET_POSTS + "/";
    private static final String URL_SEPARATOR = "/";

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
            uploadToMinio(storagePath, file, BUCKET_POSTS);
            // URL format: {minio.endpoint}/posts/{postId}/featured/{uuid}.jpg
            return generateFileUrl(BUCKET_POSTS + "/" + storagePath);
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
    public PostAttachments uploadPostAttachment(int postId, MultipartFile file) {
        validateFile(file);

        String fileUUID = UUID.randomUUID().toString();
        String extension = getFileExtension(file.getOriginalFilename());
        String storedFileName = fileUUID + extension;

        //Minio path (without bucket name): {postId}/attachments/{uuid}.ext
        String storagePath = String.format("%d/attachments/%s", postId, storedFileName);

        try {
            uploadToMinio(storagePath, file, BUCKET_POSTS);

            PostAttachments postAttachment = new PostAttachments();
            postAttachment.setPostId(postId);
            postAttachment.setOriginalFilename(file.getOriginalFilename());
            postAttachment.setStoredFilename(storedFileName);
            postAttachment.setFileType(determineFileType(file.getContentType()));
            postAttachment.setMimeType(file.getContentType());
            postAttachment.setFileSize(file.getSize());
            postAttachment.setStoragePath(storagePath);
            log.info("Saving attachment to DB: {}", postAttachment);
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
            uploadToMinio(storagePath, file, BUCKET_USERS);
            return generateFileUrl(BUCKET_USERS + "/" + storagePath);
        } catch (Exception e) {
            throw new FileUploadException("Failed to upload avatar: " + e.getMessage());
        }
    }

    /**
     * Upload inline file/image for TipTap Editor content (when post doesn't exist yet) Files are stored in temp folder
     * first, then moved to post folder after post is created Supports: images, documents, archives (zip/rar), code
     * files
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
            uploadToMinio(storagePath, file, BUCKET_POSTS);
            return generateFileUrl(BUCKET_POSTS + "/" + storagePath);
        } catch (Exception e) {
            throw new FileUploadException("Failed to upload content file: " + e.getMessage());
        }
    }

    /**
     * Move content files (images, documents, archives, etc.) from temp folder to post folder after post is created This
     * method should be called after post is saved Returns a map of old URLs to new URLs for content replacement
     *
     * @param postId   The post ID
     * @param fileUrls List of file URLs in the content
     * @return Map of old URL → new URL
     */
    public Map<String, String> moveContentFilesToPost(int postId, List<String> fileUrls) {
        Map<String, String> urlMapping = new java.util.HashMap<>();

        if (fileUrls == null || fileUrls.isEmpty()) {
            return urlMapping;
        }

        for (String fileUrl : fileUrls) {
            try {
                // Extract storage path from URL
                // URL format: {minio.endpoint}/posts/temp/content-files/uuid.jpg
                String storagePath = fileUrl.replace(minioEndpoint + URL_SEPARATOR, "");

                // Only process temp files
                if (!storagePath.startsWith(BUCKET_POSTS_PREFIX + "temp/content-files/") && !storagePath.startsWith(
                        BUCKET_POSTS_PREFIX + "temp/content-images/")) {
                    continue;
                }

                // Extract filename
                String fileName = storagePath.substring(storagePath.lastIndexOf("/") + 1);

                // Remove bucket name prefix - convert "posts/temp/..." to just "temp/..."
                String objectPath = storagePath.substring(BUCKET_POSTS_PREFIX.length());

                // Build new path: postId + "/content/" + filename
                String newObjectPath = String.format("%d/content/%s", postId, fileName);

                log.info("[MinIO] Moving file:");
                log.info("[MinIO] From: {}", objectPath);
                log.info("[MinIO] To: {}", newObjectPath);

                // Copy object to new location
                minioClient.copyObject(
                        CopyObjectArgs.builder()
                                .bucket(BUCKET_POSTS)
                                .object(newObjectPath)
                                .source(
                                        CopySource.builder()
                                                .bucket(BUCKET_POSTS)
                                                .object(objectPath)
                                                .build()
                                )
                                .build()
                );

                // Delete old object
                minioClient.removeObject(
                        RemoveObjectArgs.builder()
                                .bucket(BUCKET_POSTS)
                                .object(objectPath)
                                .build()
                );

                // Generate new URL
                String newUrl = generateFileUrl(BUCKET_POSTS_PREFIX + newObjectPath);

                // Store mapping: old URL → new URL
                urlMapping.put(fileUrl, newUrl);

                log.info("[MinIO] File moved successfully: {} → {}", fileUrl, newUrl);
            } catch (Exception e) {
                // Log error but don't throw - allow post creation to succeed
                log.error("Failed to move content file: {}", e.getMessage());
            }
        }

        return urlMapping;
    }

    /**
     * Update HTML content by replacing old file URLs with new URLs Used after moving files from temp to post folder
     *
     * @param htmlContent Original HTML content with temp URLs
     * @param urlMapping  Map of old URL → new URL
     * @return Updated HTML content with new URLs
     */
    public String updateContentUrls(String htmlContent, java.util.Map<String, String> urlMapping) {
        if (htmlContent == null || htmlContent.isEmpty() || urlMapping == null
                || urlMapping.isEmpty()) {
            return htmlContent;
        }

        String updatedContent = htmlContent;

        for (java.util.Map.Entry<String, String> entry : urlMapping.entrySet()) {
            String oldUrl = entry.getKey();
            String newUrl = entry.getValue();

            // Replace all occurrences of old URL with new URL
            updatedContent = updatedContent.replace(oldUrl, newUrl);
        }

        log.info("[Content] Updated {} URL(s) in content", urlMapping.size());
        return updatedContent;
    }

    /**
     * Delete all files associated with a post from MinIO and DB This includes: - Featured image: /{postId}/featured/ -
     * Content files: /{postId}/content/ - Attachments: /{postId}/attachments/
     *
     * @param postId The post ID
     */
    public void deletePostFile(int postId) {
        try {
            // Path prefix for this post (without bucket name)
            String folderPrefix = postId + "/";
            log.info("[MinIO] Deleting all files under: {}{}", BUCKET_POSTS_PREFIX, folderPrefix);

            // List all objects with this prefix
            Iterable<Result<Item>> results = minioClient.listObjects(
                    ListObjectsArgs.builder()
                            .bucket(BUCKET_POSTS)
                            .prefix(folderPrefix)
                            .recursive(true)
                            .build()
            );

            // Delete each object
            int deletedCount = 0;
            for (Result<Item> result : results) {
                Item item = result.get();
                String objectName = item.objectName();

                log.debug("[MinIO] Deleting object: {}", objectName);
                minioClient.removeObject(
                        RemoveObjectArgs.builder()
                                .bucket(BUCKET_POSTS)
                                .object(objectName)
                                .build()
                );
                deletedCount++;
            }

            log.info("[MinIO] Successfully deleted {} file(s) for post {}", deletedCount, postId);

            // Also delete attachment records from database
            List<PostAttachments> attachments = postAttachmentRepository.findByPostId(postId);
            if (!attachments.isEmpty()) {
                postAttachmentRepository.deleteAll(attachments);
                log.info("[DB] Deleted {} attachment record(s) from database", attachments.size());
            }
        } catch (Exception e) {
            log.error("Failed to delete post files for postId {}: {}", postId, e.getMessage(), e);
            throw new FileUploadException("Failed to delete post files: " + e.getMessage());
        }
    }



    /**
     * Get all attachments for a specific post
     *
     * @param postId The post ID
     * @return List of PostAttachment entities
     */
    public List<PostAttachments> getPostAttachments(int postId) {
        return postAttachmentRepository.findByPostId(postId);
    }

    /**
     * Delete a post attachment (removes from both DB and MinIO)
     *
     * @param postId       The post ID that owns the attachment
     * @param attachmentId The attachment ID to delete
     */
    public void deleteAttachment(int postId, int attachmentId) {
        PostAttachments postAttachment = postAttachmentRepository.findById(attachmentId)
                .orElseThrow(
                        () -> new FileUploadException("Attachment not found with id: " + attachmentId));

        // Validate that attachment belongs to the specified post
        if (postAttachment.getPostId() != postId) {
            throw new FileUploadException("Attachment " + attachmentId + " does not belong to post " + postId);
        }

        try {
            // Delete from MinIO storage
            minioClient.removeObject(
                    RemoveObjectArgs.builder()
                            .bucket(BUCKET_POSTS)
                            .object(postAttachment.getStoragePath())
                            .build()
            );

            // Delete from database
            postAttachmentRepository.deleteById(attachmentId);
        } catch (Exception e) {
            throw new FileUploadException("Failed to delete attachment: " + e.getMessage());
        }
    }


    /**
     * Delete a file from MinIO by its URL Extracts bucket and object name from URL and deletes the file
     *
     * @param fileUrl Full URL of the file (e.g., <a
     *                href="{minio.endpoint}/posts/123/featured/file.jpg">...</a>)
     */
    public void deleteFileByUrl(String fileUrl) {
        if (fileUrl == null || fileUrl.trim().isEmpty()) {
            log.warn("[MinIO] Cannot delete file: URL is null or empty");
            return;
        }

        try {
            // Extract bucket and object path from URL
            // URL format: {minio.endpoint}/BUCKET/OBJECT_PATH
            // Example: {minio.endpoint}/posts/123/featured/file.jpg
            //   → bucket: "posts"
            //   → object: "123/featured/file.jpg"

            String urlPath = fileUrl;

            // Remove protocol and domain
            if (urlPath.contains("://")) {
                urlPath = urlPath.substring(urlPath.indexOf("://") + 3); // Remove "https://" or "http://"
            }
            if (urlPath.contains("/")) {
                urlPath = urlPath.substring(urlPath.indexOf("/") + 1); // Remove domain
            }

            // Extract bucket (first segment) and object path (rest)
            String[] parts = urlPath.split("/", 2);
            if (parts.length < 2) {
                log.error("[MinIO] Invalid file URL format: {}", fileUrl);
                return;
            }

            String bucket = parts[0];
            String objectName = parts[1];

            log.info("[MinIO] Deleting file from bucket '{}': {}", bucket, objectName);

            minioClient.removeObject(
                    RemoveObjectArgs.builder()
                            .bucket(bucket)
                            .object(objectName)
                            .build()
            );

            log.info("[MinIO] Successfully deleted file: {}", fileUrl);
        } catch (Exception e) {
            log.error("[MinIO] Failed to delete file by URL: {}", fileUrl, e);
            // Don't throw exception - continue with other operations
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

        // Get max file size and allowed extensions based on category
        record FileCategoryConfig(long maxSize, List<String> allowedExtensions) {}

        FileCategoryConfig config = switch (fileCategory) {
            case "image" -> new FileCategoryConfig(MAX_IMAGE_SIZE, ALLOWED_IMAGE_EXTENSIONS);
            case "document" -> new FileCategoryConfig(MAX_DOCUMENT_SIZE, ALLOWED_DOCUMENT_EXTENSIONS);
            case "archive" -> new FileCategoryConfig(MAX_ARCHIVE_SIZE, ALLOWED_ARCHIVE_EXTENSIONS);
            case "code" -> new FileCategoryConfig(MAX_CODE_SIZE, ALLOWED_CODE_EXTENSIONS);
            default -> throw new FileUploadException("File type not supported: " + extension);
        };

        // Validate file size
        if (file.getSize() > config.maxSize()) {
            throw new FileUploadException(
                    String.format("File size exceeds maximum allowed size of %d MB for %s files",
                            config.maxSize() / (1024 * 1024), fileCategory)
            );
        }

        // Validate extension
        if (!config.allowedExtensions().contains(extension)) {
            throw new FileUploadException(
                    String.format("File extension %s not allowed. Allowed %s types: %s",
                            extension, fileCategory, config.allowedExtensions())
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
            return "UNKNOWN";
        } else if (contentType.startsWith("image/")) {
            return "IMAGE";
        } else if (contentType.startsWith("video/")) {
            return "VIDEO";
        } else if (contentType.startsWith("audio/")) {
            return "AUDIO";
        } else if (contentType.equals("application/pdf")) {
            return "DOCUMENT";
        } else {
            return "OTHER";
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
        log.debug("[MinIO] Checking bucket: {}", bucketName);
        boolean exists = minioClient.bucketExists(
                BucketExistsArgs.builder()
                        .bucket(bucketName)
                        .build()
        );

        if (!exists) {
            log.info("[MinIO] Bucket not found, creating: {}", bucketName);
            minioClient.makeBucket(
                    MakeBucketArgs.builder()
                            .bucket(bucketName)
                            .build()
            );
            log.info("[MinIO] Successfully created bucket: {}", bucketName);
        } else {
            log.debug("[MinIO] Bucket already exists: {}", bucketName);
        }
    }

    private void uploadToMinio(String storagePath, MultipartFile file, String bucketName)
            throws Exception {
        log.debug("[MinIO] === Upload Start ===");
        log.debug("[MinIO] Bucket: {}", bucketName);
        log.debug("[MinIO] Storage Path: {}", storagePath);
        log.debug("[MinIO] File Name: {}", file.getOriginalFilename());

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
            log.debug("[MinIO] Upload successful");
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
     * Extract all file URLs from HTML content (from TipTap Editor) Looks for: - <img> tags with src attribute - <a>
     * tags with href attribute pointing to files
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
        // Use simpler regex and validate extension in code
        java.util.regex.Pattern linkPattern = java.util.regex.Pattern.compile(
                "<a[^>]+href=\"([^\"]+)\"",
                java.util.regex.Pattern.CASE_INSENSITIVE
        );
        java.util.regex.Matcher linkMatcher = linkPattern.matcher(htmlContent);
        while (linkMatcher.find()) {
            String url = linkMatcher.group(1);
            // Only add URLs from our MinIO endpoint with valid file extensions
            if (url.startsWith(minioEndpoint) && hasAllowedFileExtension(url)) {
                fileUrls.add(url);
            }
        }

        // Pattern to match Markdown images: ![alt](url)
        java.util.regex.Pattern mdImgPattern = java.util.regex.Pattern.compile(
                "!\\[[^\\]]*\\]\\(([^\\)]+)\\)"
        );
        java.util.regex.Matcher mdImgMatcher = mdImgPattern.matcher(htmlContent);
        while (mdImgMatcher.find()) {
            String url = mdImgMatcher.group(1);
            if (url.startsWith(minioEndpoint)) {
                fileUrls.add(url);
            }
        }

        // Pattern to match Markdown links: [text](url)
        java.util.regex.Pattern mdLinkPattern = java.util.regex.Pattern.compile(
                "(?<!!)\\[[^\\]]*\\]\\(([^\\)]+)\\)"
        );
        java.util.regex.Matcher mdLinkMatcher = mdLinkPattern.matcher(htmlContent);
        while (mdLinkMatcher.find()) {
            String url = mdLinkMatcher.group(1);
            if (url.startsWith(minioEndpoint) && hasAllowedFileExtension(url)) {
                fileUrls.add(url);
            }
        }

        return fileUrls;
    }

    /**
     * Check if URL has an allowed file extension (documents, archives, code files)
     */
    private boolean hasAllowedFileExtension(String url) {
        String lowerUrl = url.toLowerCase();
        return ALLOWED_DOCUMENT_EXTENSIONS.stream().anyMatch(lowerUrl::endsWith)
                || ALLOWED_ARCHIVE_EXTENSIONS.stream().anyMatch(lowerUrl::endsWith)
                || ALLOWED_CODE_EXTENSIONS.stream().anyMatch(lowerUrl::endsWith);
    }
}
