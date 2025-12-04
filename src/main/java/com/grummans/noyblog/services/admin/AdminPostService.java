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
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminPostService {

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

            postDTORes.setTags(tags.stream().map(tagMapper::toTagSimpleDTO).collect(Collectors.toList()));

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
        // Process content
        String htmlContent = req.getContent();
        if (htmlContent == null || htmlContent.trim().isEmpty()) {
            htmlContent = "";
        }
        String sanitizedHtml = contentService.sanitizeHtml(htmlContent);

        Posts post;
        boolean isUpdate = false;

        // Check if updating existing draft or creating new one
        if (req.getId() != null && req.getId() > 0) {
            // UPDATE existing draft
            post = postRepository.findById(req.getId())
                    .orElseThrow(() -> new IllegalArgumentException("Draft post not found with id: " + req.getId()));

            // Only allow updating drafts, not published posts
            if (!"DRAFT".equals(post.getStatus())) {
                throw new IllegalArgumentException("Cannot save as draft - post is already published");
            }

            isUpdate = true;
        } else {
            // CREATE new draft
            post = postMapper.toPost(req);
            int authorId = usersRepository.findIdByUsername(req.getAuthorUsername());
            post.setAuthorId(authorId);
        }

        // Set draft status and content
        post.setStatus("DRAFT");
        post.setContent(sanitizedHtml);
        post.setContentHtml(sanitizedHtml);

        // Update other fields (title, excerpt, etc.)
        if (req.getTitle() != null) post.setTitle(req.getTitle());
        if (req.getExcerpt() != null) post.setExcerpt(req.getExcerpt());
        if (req.getSlug() != null) post.setSlug(req.getSlug());

        // Handle category: Allow null for drafts
        if (req.getCategoryId() > 0) {
            // Valid category provided
            post.setCategoryId(req.getCategoryId());
        } else if (!isUpdate) {
            // New draft without category - set to null
            post.setCategoryId(null);
        }
        // For updates: if no categoryId provided or 0, keep existing value (don't change)

        if (req.getMetaTitle() != null) post.setMetaTitle(req.getMetaTitle());
        if (req.getMetaDescription() != null) post.setMetaDescription(req.getMetaDescription());
        if (req.getReadingTimeMinutes() > 0) post.setReadingTimeMinutes(req.getReadingTimeMinutes());

        // Save post
        Posts savedPost = postRepository.save(post);

        // Upload featured image if provided
        if (featuredImage != null && !featuredImage.isEmpty()) {
            String featuredImageUrl = fileService.uploadFeaturedImage(savedPost.getId(), featuredImage);
            savedPost.setFeaturedImageUrl(featuredImageUrl);
            savedPost = postRepository.save(savedPost);
        }

        // Move content files from temp to post folder and update URLs in content
        if (!sanitizedHtml.isEmpty()) {
            List<String> fileUrls = fileService.extractFileUrlsFromContent(sanitizedHtml);
            Map<String, String> urlMapping = fileService.moveContentFilesToPost(savedPost.getId(), fileUrls);

            // Update content with new URLs (replace temp URLs with post-specific URLs)
            if (!urlMapping.isEmpty()) {
                String updatedContent = fileService.updateContentUrls(sanitizedHtml, urlMapping);
                savedPost.setContent(updatedContent);
                savedPost.setContentHtml(updatedContent);
                savedPost = postRepository.save(savedPost);
                log.info("Updated content URLs for draft post {}", savedPost.getId());
            }
        }

        // Handle tags (delete old ones if updating, add new ones)
        if (req.getTagId() != null && !req.getTagId().isEmpty()) {
            if (isUpdate) {
                // Delete all existing tags for this post
                postTagsRepository.deleteAllByPostId(savedPost.getId());
            }

            // Add new tags
            for (Integer tagId : req.getTagId()) {
                Tags tag = tagsRepository.findById(tagId)
                        .orElseThrow(() -> new IllegalArgumentException("Tag not found with id: " + tagId));

                PostTags postTag = new PostTags();
                postTag.setPost(savedPost);
                postTag.setTag(tag);
                postTagsRepository.save(postTag);
            }
        }

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

        // Validate required fields before publishing
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

        // Process content: FE sends HTML from Tip Tap Editor
        String htmlContent = req.getContent();
        if (htmlContent == null || htmlContent.trim().isEmpty()) {
            htmlContent = ""; // Allow empty content for now (can be tightened later)
        }

        // Sanitize HTML to prevent XSS
        String sanitizedHtml = contentService.sanitizeHtml(htmlContent);

        Posts post;
        if (isUpdate) {
            // UPDATE existing draft post
            post = postRepository.findById(req.getId())
                    .orElseThrow(() -> new IllegalArgumentException("Post not found with id: " + req.getId()));

            if (!"DRAFT".equals(post.getStatus())) {
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
            // CREATE new post
            post = postMapper.toPost(req);
            post.setContent(sanitizedHtml);
            post.setContentHtml(sanitizedHtml);

            int authorId = usersRepository.findIdByUsername(req.getAuthorUsername());
            post.setAuthorId(authorId);
        }

        // Set published status and date (for both create and update)
        post.setStatus("PUBLISHED");
        post.setPublishedAt(java.time.LocalDateTime.now());

        // Save post
        Posts publishedPost = postRepository.save(post);

        // Upload featured image if provided
        if (featuredImage != null && !featuredImage.isEmpty()) {
            String featuredImageUrl = fileService.uploadFeaturedImage(publishedPost.getId(), featuredImage);
            publishedPost.setFeaturedImageUrl(featuredImageUrl);
            publishedPost = postRepository.save(publishedPost);
        }

        // Move content files (images, documents, archives) from temp to post folder and update URLs
        if (!sanitizedHtml.isEmpty()) {
            List<String> fileUrls = fileService.extractFileUrlsFromContent(sanitizedHtml);
            Map<String, String> urlMapping = fileService.moveContentFilesToPost(publishedPost.getId(), fileUrls);

            // Update content with new URLs (replace temp URLs with post-specific URLs)
            if (!urlMapping.isEmpty()) {
                String updatedContent = fileService.updateContentUrls(sanitizedHtml, urlMapping);
                publishedPost.setContent(updatedContent);
                publishedPost.setContentHtml(updatedContent);
                publishedPost = postRepository.save(publishedPost);
                log.info("Updated content URLs for published post {}", publishedPost.getId());
            }
        }

        // Update tags: Delete old ones (if update) and add new ones
        if (isUpdate) {
            postTagsRepository.deleteAllByPostId(publishedPost.getId());
        }
        for (Integer tagId : req.getTagId()) {
            Tags tag = tagsRepository.findById(tagId)
                    .orElseThrow(() -> new IllegalArgumentException("Tag not found with id: " + tagId));

            PostTags postTag = new PostTags();
            postTag.setPost(publishedPost);
            postTag.setTag(tag);
            postTagsRepository.save(postTag);
        }

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
        // Find existing published post
        Posts post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("Post not found with id: " + postId));

        if (!"PUBLISHED".equals(post.getStatus())) {
            throw new IllegalArgumentException("Cannot update - post is not published. Current status: " + post.getStatus() + ". Use /save-draft for drafts.");
        }

        // Validate required fields
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

        // *** IMPORTANT: Capture old content BEFORE updating post object ***
        // This is needed to detect which files are no longer used and should be deleted
        String oldContentBeforeUpdate = post.getContent();
        List<String> oldFileUrls = fileService.extractFileUrlsFromContent(oldContentBeforeUpdate != null ? oldContentBeforeUpdate : "");

        log.debug("Old content file URLs (before update): {}", oldFileUrls);

        // Process and sanitize content
        String htmlContent = req.getContent();
        if (htmlContent == null || htmlContent.trim().isEmpty()) {
            htmlContent = "";
        }
        String sanitizedHtml = contentService.sanitizeHtml(htmlContent);

        // Update all fields
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

        // Keep status as PUBLISHED (don't change)
        // Keep publishedAt (don't change)
        // updatedAt will be updated automatically by @UpdateTimestamp

        // Save post
        Posts updatedPost = postRepository.save(post);

        // Upload new featured image if provided
        if (featuredImage != null && !featuredImage.isEmpty()) {
            // Delete old featured image first
            if (updatedPost.getFeaturedImageUrl() != null) {
                fileService.deleteFileByUrl(updatedPost.getFeaturedImageUrl());
            }

            String featuredImageUrl = fileService.uploadFeaturedImage(updatedPost.getId(), featuredImage);
            updatedPost.setFeaturedImageUrl(featuredImageUrl);
            updatedPost = postRepository.save(updatedPost);
        }

        // Handle content files - cleanup old files and move new files
        List<String> newFileUrls = fileService.extractFileUrlsFromContent(sanitizedHtml);
        log.debug("New content file URLs (after update): {}", newFileUrls);

        // Delete files that are no longer referenced
        for (String oldUrl : oldFileUrls) {
            if (!newFileUrls.contains(oldUrl)) {
                log.info("Deleting unused file: {}", oldUrl);
                fileService.deleteFileByUrl(oldUrl);
            }
        }

        // Move new files from temp to post folder and update URLs
        if (!sanitizedHtml.isEmpty()) {
            Map<String, String> urlMapping = fileService.moveContentFilesToPost(updatedPost.getId(), newFileUrls);

            // Update content with new URLs (replace temp URLs with post-specific URLs)
            if (!urlMapping.isEmpty()) {
                String updatedContent = fileService.updateContentUrls(sanitizedHtml, urlMapping);
                updatedPost.setContent(updatedContent);
                updatedPost.setContentHtml(updatedContent);
                updatedPost = postRepository.save(updatedPost);
                log.info("Updated content URLs for published post {}", updatedPost.getId());
            }
        }

        // Update tags: Delete old ones and add new ones
        postTagsRepository.deleteAllByPostId(updatedPost.getId());
        for (Integer tagId : req.getTagId()) {
            Tags tag = tagsRepository.findById(tagId)
                    .orElseThrow(() -> new IllegalArgumentException("Tag not found with id: " + tagId));

            PostTags postTag = new PostTags();
            postTag.setPost(updatedPost);
            postTag.setTag(tag);
            postTagsRepository.save(postTag);
        }

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
                .orElseThrow(() -> new IllegalArgumentException("Post not found with id: " + postId));

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
                .collect(Collectors.toList()));

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
                .orElseThrow(() -> new IllegalArgumentException("Post not found with id: " + postId));

        fileService.deletePostFile(postId);

        // Delete the post
        postRepository.delete(post);

    }
}
