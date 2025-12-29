package com.grummans.noyblog.services.admin;

import com.grummans.noyblog.dto.CategoryDTO;
import com.grummans.noyblog.dto.PostDTO;
import com.grummans.noyblog.dto.UserDTO;
import com.grummans.noyblog.mapper.CategoryMapper;
import com.grummans.noyblog.mapper.PostMapper;
import com.grummans.noyblog.mapper.TagMapper;
import com.grummans.noyblog.mapper.UserMapper;
import com.grummans.noyblog.model.*;
import com.grummans.noyblog.repository.*;
import com.grummans.noyblog.services.system.FileService;
import com.grummans.noyblog.services.system.ContentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminPostService {

    // Constants for status
    private static final String STATUS_DRAFT = "DRAFT";
    private static final String STATUS_PUBLISHED = "PUBLISHED";

    // Constants for error messages
    private static final String ERROR_POST_NOT_FOUND = "Post not found with id: ";
    private static final String ERROR_TAG_NOT_FOUND = "Tag not found with id: ";

    private final PostRepository postRepository;

    private final PostTagsRepository postTagsRepository;

    private final UsersRepository usersRepository;

    private final TagsRepository tagsRepository;

    private final CategoryRepository categoryRepository;

    private final PostMapper postMapper;

    private final UserMapper userMapper;

    private final CategoryMapper categoryMapper;

    private final TagMapper tagMapper;

    private final FileService fileService;

    private final ContentService contentService;

    /**
     * Get all posts with optional title and status filtering and pagination.
     *
     * @param req:  PostDTO.Req object containing filter criteria (title, status)
     * @param page: page number (0-based)
     * @param size: number of items per page
     * @return Page of PostDTO.Res objects
     */
    public Page<PostDTO.Res> getAllPost(PostDTO.Req req, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());

        Page<Posts> postsList;

        // Filter by both title and status
        String title = req.getTitle();
        String status = req.getStatus();

        if (title != null && !title.isEmpty() && status != null && !status.isEmpty()) {
            // Both filters
            postsList = postRepository.findByTitleContainingIgnoreCaseAndStatus(title, status, pageable);
        } else if (title != null && !title.isEmpty()) {
            // Title only
            postsList = postRepository.findByTitleContainingIgnoreCase(title, pageable);
        } else if (status != null && !status.isEmpty()) {
            // Status only
            postsList = postRepository.findByStatus(status, pageable);
        } else {
            // No filter
            postsList = postRepository.findAll(pageable);
        }

        return postsList.map(post -> {

            PostDTO.Res postDTORes = postMapper.toPostDTO(post);

            Users author = usersRepository.findById(post.getAuthorId())
                    .orElseThrow(() -> new IllegalArgumentException("Author not found with id: " + post.getAuthorId()));
            UserDTO.AuthorDTO authorDTO = userMapper.toAuthorDTO(author);

            postDTORes.setAuthor(authorDTO);

            // Handle category - can be null for drafts
            if (post.getCategoryId() != null && post.getCategoryId() > 0) {
                Categories category = categoryRepository.findById(post.getCategoryId())
                        .orElseThrow(() -> new IllegalArgumentException("Category not found with id: " + post.getCategoryId()));
                CategoryDTO.CategorySimpleDTO categoryDTO = categoryMapper.toCategorySimpleDTO(category);
                postDTORes.setCategory(categoryDTO);
            } else {
                postDTORes.setCategory(null);  // Draft without category
            }

            List<Tags> tags = postTagsRepository.findByPostId(post.getId());

            postDTORes.setTags(tags.stream().map(tagMapper::toTagSimpleDTO).toList());

            return postDTORes;
        });
    }

    /**
     * Save or update a draft post
     * - If post ID exists: UPDATE existing draft
     * - If post ID is null/0: CREATE new draft
     * <p>
     * Drafts can be saved partially (e.g., without tags, category, etc.)
     *
     * @param req           PostDTO.Req with draft data
     * @param featuredImage Optional featured image
     * @return Saved draft post
     */
    @Transactional
    public PostDTO.SimplePostDTO saveDraftPost(PostDTO.Req req, MultipartFile featuredImage) {
        req.setAuthorUsername("grummans");
        String sanitizedHtml = sanitizeContent(req.getContent());

        Posts post;
        boolean isUpdate = req.getId() != null && req.getId() > 0;

        if (isUpdate) {
            post = postRepository.findById(req.getId())
                    .orElseThrow(() -> new IllegalArgumentException(ERROR_POST_NOT_FOUND + req.getId()));

            if (!STATUS_DRAFT.equals(post.getStatus())) {
                throw new IllegalArgumentException("Cannot save as draft - post is already published");
            }
        } else {
            post = postMapper.toPost(req);
            int authorId = usersRepository.findIdByUsername(req.getAuthorUsername());
            post.setAuthorId(authorId);
        }

        post.setStatus(STATUS_DRAFT);
        post.setContent(sanitizedHtml);
        post.setContentHtml(sanitizedHtml);
        updateDraftFields(post, req, isUpdate);

        Posts savedPost = postRepository.save(post);
        savedPost = handleFeaturedImageUpload(savedPost, featuredImage);
        savedPost = processContentFiles(savedPost, sanitizedHtml);
        updatePostTags(savedPost.getId(), req.getTagId(), isUpdate);

        return postMapper.toSimplePostDTO(savedPost);
    }

    /**
     * Save and publish a post (create new OR update draft, then publish)
     * - If req. Id is null/0: Create NEW post with PUBLISHED status
     * - If req.id exists: Update DRAFT post and change to PUBLISHED status
     * - Validates all required fields before publishing
     * - Handles featured image upload and content file migration
     *
     * @param req           PostDTO.Req with complete post data (with or without ID)
     * @param featuredImage Optional featured image
     * @return Published post
     */
    @Transactional
    public PostDTO.SimplePostDTO saveAndPublishPost(PostDTO.Req req, MultipartFile featuredImage) {
        boolean isUpdate = req.getId() != null && req.getId() > 0;

        validatePublishFields(req);

        String sanitizedHtml = sanitizeContent(req.getContent());

        Posts post;
        if (isUpdate) {
            post = postRepository.findById(req.getId())
                    .orElseThrow(() -> new IllegalArgumentException(ERROR_POST_NOT_FOUND + req.getId()));

            if (!STATUS_DRAFT.equals(post.getStatus())) {
                throw new IllegalArgumentException("Cannot publish - post is not a draft. Current status: " + post.getStatus());
            }

            // Update all fields with complete data
            post.setTitle(req.getTitle());
            post.setExcerpt(req.getExcerpt());
            post.setSlug(req.getSlug());
            post.setCategoryId(req.getCategoryId());
            post.setContent(sanitizedHtml);
            post.setContentHtml(sanitizedHtml);
            post.setMetaTitle(req.getMetaTitle());
            post.setMetaDescription(req.getMetaDescription());
            post.setReadingTimeMinutes(req.getReadingTimeMinutes());
            post.setFeatured(req.isFeatured());

        } else {
            post = postMapper.toPost(req);
            post.setContent(sanitizedHtml);
            post.setContentHtml(sanitizedHtml);

            int authorId = usersRepository.findIdByUsername(req.getAuthorUsername());
            post.setAuthorId(authorId);
        }

        post.setStatus(STATUS_PUBLISHED);
        post.setPublishedAt(java.time.LocalDateTime.now());

        Posts publishedPost = postRepository.save(post);
        publishedPost = handleFeaturedImageUpload(publishedPost, featuredImage);
        publishedPost = processContentFiles(publishedPost, sanitizedHtml);
        updatePostTags(publishedPost.getId(), req.getTagId(), isUpdate);

        return postMapper.toSimplePostDTO(publishedPost);
    }

    /**
     * Update an already published post
     * - Only works with posts that have status = PUBLISHED
     * - Validates all required fields
     * - Updates content, tags, category, featured image, etc.
     * - Keeps publishedAt date, updates updatedAt automatically
     *
     * @param postId        Post ID to update
     * @param req           PostDTO.Req with updated post data
     * @param featuredImage Optional new featured image
     * @return Updated post
     */
    @Transactional
    public PostDTO.SimplePostDTO updatePublishedPost(int postId, PostDTO.Req req, MultipartFile featuredImage) {
        Posts post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException(ERROR_POST_NOT_FOUND + postId));

        if (!STATUS_PUBLISHED.equals(post.getStatus())) {
            throw new IllegalArgumentException("Cannot update - post is not published. Current status: " + post.getStatus() + ". Use /save-draft for drafts.");
        }

        validateUpdateFields(req);

        // Capture old content for cleanup
        List<String> oldFileUrls = fileService.extractFileUrlsFromContent(
                post.getContent() != null ? post.getContent() : "");

        String sanitizedHtml = sanitizeContent(req.getContent());
        updatePostFields(post, req, sanitizedHtml);

        Posts updatedPost = postRepository.save(post);
        updatedPost = handleFeaturedImageUpdate(updatedPost, featuredImage);
        updatedPost = cleanupAndProcessContentFiles(updatedPost, sanitizedHtml, oldFileUrls);
        updatePostTags(updatedPost.getId(), req.getTagId(), true);

        log.info("Published post {} updated successfully", updatedPost.getId());
        return postMapper.toSimplePostDTO(updatedPost);
    }

    /**
     * Get post detail for display
     */
    public PostDTO.Res detailPost(int postId) {
        return getPostDetail(postId);
    }

    /**
     * Get post detail for editing (returns same data - TipTap Editor uses contentHtml)
     */
    public PostDTO.Res detailPostForEdit(int postId) {
        return getPostDetail(postId);
    }

    /**
     * Common method to get post detail (used for both display and editing)
     *
     * @param postId The post ID
     * @return PostDTO.Res with detailed post data
     */
    private PostDTO.Res getPostDetail(int postId) {
        Posts post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException(ERROR_POST_NOT_FOUND + postId));

        PostDTO.Res postDTORes = postMapper.toPostDTO(post);

        // Both editing and display use contentHtml (HTML format)
        // TipTap Editor can load and edit HTML directly
        // Note: The mapper ignores 'content' field, so contentHtml is the only content field returned

        Users author = usersRepository.findById(post.getAuthorId())
                .orElseThrow(() -> new IllegalArgumentException("Author not found with id: " + post.getAuthorId()));
        UserDTO.AuthorDTO authorDTO = userMapper.toAuthorDTO(author);
        postDTORes.setAuthor(authorDTO);

        // Handle category - can be null for drafts
        if (post.getCategoryId() != null && post.getCategoryId() > 0) {
            Categories category = categoryRepository.findById(post.getCategoryId())
                    .orElseThrow(() -> new IllegalArgumentException("Category not found with id: " + post.getCategoryId()));
            CategoryDTO.CategorySimpleDTO categoryDTO = categoryMapper.toCategorySimpleDTO(category);
            postDTORes.setCategory(categoryDTO);
        } else {
            postDTORes.setCategory(null);  // Draft without category
        }

        List<Tags> tags = postTagsRepository.findByPostId(post.getId());
        postDTORes.setTags(tags.stream()
                .map(tagMapper::toTagSimpleDTO)
                .toList());

        // Get post attachments (files displayed separately from content)
        List<PostAttachments> attachments = fileService.getPostAttachments(postId);
        postDTORes.setAttachments(attachments);

        return postDTORes;
    }

    /**
     * Delete a post by ID
     * - Deletes associated files from storage
     * - Deletes post record from database
     *
     * @param postId The post ID to delete
     */
    @Transactional
    public void deletePost(int postId) {
        Posts post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException(ERROR_POST_NOT_FOUND + postId));

        fileService.deletePostFile(postId);

        // Delete the post
        postRepository.delete(post);

    }

    // ==================== HELPER METHODS ====================

    /**
     * Sanitize HTML content, returning empty string if null or blank
     */
    private String sanitizeContent(String htmlContent) {
        if (htmlContent == null || htmlContent.trim().isEmpty()) {
            return "";
        }
        return contentService.sanitizeHtml(htmlContent);
    }

    /**
     * Upload featured image if provided and save the post with new URL
     */
    private Posts handleFeaturedImageUpload(Posts post, MultipartFile featuredImage) {
        if (featuredImage != null && !featuredImage.isEmpty()) {
            String featuredImageUrl = fileService.uploadFeaturedImage(post.getId(), featuredImage);
            post.setFeaturedImageUrl(featuredImageUrl);
            return postRepository.save(post);
        }
        return post;
    }

    /**
     * Move content files from temp to post folder and update URLs in content
     */
    private Posts processContentFiles(Posts post, String sanitizedHtml) {
        if (sanitizedHtml.isEmpty()) {
            return post;
        }

        List<String> fileUrls = fileService.extractFileUrlsFromContent(sanitizedHtml);
        Map<String, String> urlMapping = fileService.moveContentFilesToPost(post.getId(), fileUrls);

        if (!urlMapping.isEmpty()) {
            String updatedContent = fileService.updateContentUrls(sanitizedHtml, urlMapping);
            post.setContent(updatedContent);
            post.setContentHtml(updatedContent);
            post = postRepository.save(post);
            log.info("Updated content URLs for post {}", post.getId());
        }
        return post;
    }

    /**
     * Update post tags - delete old ones and add new ones
     */
    private void updatePostTags(int postId, List<Integer> tagIds, boolean deleteExisting) {
        if (tagIds == null || tagIds.isEmpty()) {
            return;
        }

        if (deleteExisting) {
            postTagsRepository.deleteAllByPostId(postId);
        }

        Posts post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException(ERROR_POST_NOT_FOUND + postId));

        for (Integer tagId : tagIds) {
            Tags tag = tagsRepository.findById(tagId)
                    .orElseThrow(() -> new IllegalArgumentException(ERROR_TAG_NOT_FOUND + tagId));

            PostTags postTag = new PostTags();
            postTag.setPost(post);
            postTag.setTag(tag);
            postTagsRepository.save(postTag);
        }
    }

    /**
     * Update draft post fields from request
     */
    private void updateDraftFields(Posts post, PostDTO.Req req, boolean isUpdate) {
        if (req.getTitle() != null) post.setTitle(req.getTitle());
        if (req.getExcerpt() != null) post.setExcerpt(req.getExcerpt());
        if (req.getSlug() != null) post.setSlug(req.getSlug());

        // Handle category: Allow null for drafts
        if (req.getCategoryId() > 0) {
            post.setCategoryId(req.getCategoryId());
        } else if (!isUpdate) {
            post.setCategoryId(null);
        }

        if (req.getMetaTitle() != null) post.setMetaTitle(req.getMetaTitle());
        if (req.getMetaDescription() != null) post.setMetaDescription(req.getMetaDescription());
        if (req.getReadingTimeMinutes() > 0) post.setReadingTimeMinutes(req.getReadingTimeMinutes());
    }

    /**
     * Validate required fields for publishing
     */
    private void validatePublishFields(PostDTO.Req req) {
        if (req.getTitle() == null || req.getTitle().trim().isEmpty()) {
            throw new IllegalArgumentException("Cannot publish: Title is required");
        }
        if (req.getSlug() == null || req.getSlug().trim().isEmpty()) {
            throw new IllegalArgumentException("Cannot publish: Slug is required");
        }
        if (req.getCategoryId() <= 0) {
            throw new IllegalArgumentException("Cannot publish: Category is required");
        }
        if (req.getTagId() == null || req.getTagId().isEmpty()) {
            throw new IllegalArgumentException("Cannot publish: At least one tag is required");
        }
    }

    /**
     * Validate required fields for updating published post
     */
    private void validateUpdateFields(PostDTO.Req req) {
        if (req.getTitle() == null || req.getTitle().trim().isEmpty()) {
            throw new IllegalArgumentException("Title is required");
        }
        if (req.getSlug() == null || req.getSlug().trim().isEmpty()) {
            throw new IllegalArgumentException("Slug is required");
        }
        if (req.getCategoryId() <= 0) {
            throw new IllegalArgumentException("Category is required");
        }
        if (req.getTagId() == null || req.getTagId().isEmpty()) {
            throw new IllegalArgumentException("At least one tag is required");
        }
    }

    /**
     * Update all post fields from request (for published post update)
     */
    private void updatePostFields(Posts post, PostDTO.Req req, String sanitizedHtml) {
        post.setTitle(req.getTitle());
        post.setExcerpt(req.getExcerpt());
        post.setSlug(req.getSlug());
        post.setCategoryId(req.getCategoryId());
        post.setContent(sanitizedHtml);
        post.setContentHtml(sanitizedHtml);
        post.setMetaTitle(req.getMetaTitle());
        post.setMetaDescription(req.getMetaDescription());
        post.setReadingTimeMinutes(req.getReadingTimeMinutes());
        post.setFeatured(req.isFeatured());
    }

    /**
     * Handle featured image update - delete old and upload new
     */
    private Posts handleFeaturedImageUpdate(Posts post, MultipartFile featuredImage) {
        if (featuredImage != null && !featuredImage.isEmpty()) {
            // Delete old featured image first
            if (post.getFeaturedImageUrl() != null) {
                fileService.deleteFileByUrl(post.getFeaturedImageUrl());
            }
            String featuredImageUrl = fileService.uploadFeaturedImage(post.getId(), featuredImage);
            post.setFeaturedImageUrl(featuredImageUrl);
            return postRepository.save(post);
        }
        return post;
    }

    /**
     * Cleanup old content files and process new content files
     */
    private Posts cleanupAndProcessContentFiles(Posts post, String sanitizedHtml, List<String> oldFileUrls) {
        List<String> newFileUrls = fileService.extractFileUrlsFromContent(sanitizedHtml);

        // Delete files that are no longer referenced
        for (String oldUrl : oldFileUrls) {
            if (!newFileUrls.contains(oldUrl)) {
                log.info("Deleting unused file: {}", oldUrl);
                fileService.deleteFileByUrl(oldUrl);
            }
        }

        // Move new files from temp to post folder and update URLs
        return processContentFiles(post, sanitizedHtml);
    }
}
