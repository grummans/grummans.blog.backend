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
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class PostService {

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
     * @param req PostDTO.Req with draft data
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
            log.info("Updating existing draft post with ID: {}", req.getId());
        } else {
            // CREATE new draft
            post = postMapper.toPost(req);
            int authorId = usersRepository.findIdByUsername(req.getAuthorUsername());
            post.setAuthorId(authorId);
            log.info("Creating new draft post");
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

        // Move content files from temp to post folder
        if (!sanitizedHtml.isEmpty()) {
            List<String> fileUrls = fileService.extractFileUrlsFromContent(sanitizedHtml);
            fileService.moveContentFilesToPost(savedPost.getId(), fileUrls);
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

        log.info("Draft post saved successfully with ID: {}", savedPost.getId());
        return postMapper.toSimplePostDTO(savedPost);
    }

    /**
     * Publish a draft post (convert DRAFT status to PUBLISHED)
     *
     * @param postId The draft post ID
     * @return Published post
     */
    @Transactional
    public PostDTO.SimplePostDTO publishDraft(int postId) {
        Posts post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("Post not found with id: " + postId));

        if (!"DRAFT".equals(post.getStatus())) {
            throw new IllegalArgumentException("Post is not a draft. Current status: " + post.getStatus());
        }

        // Validate required fields before publishing
        if (post.getTitle() == null || post.getTitle().trim().isEmpty()) {
            throw new IllegalArgumentException("Cannot publish: Title is required");
        }
        if (post.getSlug() == null || post.getSlug().trim().isEmpty()) {
            throw new IllegalArgumentException("Cannot publish: Slug is required");
        }
        if (post.getCategoryId() == null || post.getCategoryId() <= 0) {
            throw new IllegalArgumentException("Cannot publish: Category is required");
        }

        // Update status and published date
        post.setStatus("PUBLISHED");
        post.setPublishedAt(java.time.LocalDateTime.now());

        Posts publishedPost = postRepository.save(post);
        log.info("Draft post {} published successfully", postId);

        return postMapper.toSimplePostDTO(publishedPost);
    }

    /**
     * Create and publish a post directly (skip draft)
     *
     * @param req PostDTO.Req with post data
     * @param featuredImage Optional featured image
     * @return Created post
     */
    @Transactional
    public PostDTO.SimplePostDTO createPost(PostDTO.Req req, MultipartFile featuredImage) {
        // Process content: FE sends HTML from Tip Tap Editor
        // TipTap Editor works with HTML, not Markdown, so we save HTML in both fields

        String htmlContent = req.getContent();
        if (htmlContent == null || htmlContent.trim().isEmpty()) {
            htmlContent = ""; // Ensure non-null for DB constraint
        }

        // Sanitize HTML to prevent XSS
        String sanitizedHtml = contentService.sanitizeHtml(htmlContent);

        // Create post entity
        Posts post = postMapper.toPost(req);

        // Save HTML in both fields:
        // - content: HTML for TipTap Editor (editing)
        // - contentHtml: HTML for display (cached)
        post.setContent(sanitizedHtml);        // HTML for editing with TipTap
        post.setContentHtml(sanitizedHtml);    // HTML for display

        int authorId = usersRepository.findIdByUsername(req.getAuthorUsername());
        post.setAuthorId(authorId);

        // Set published status and date
        post.setStatus("PUBLISHED");
        post.setPublishedAt(java.time.LocalDateTime.now());

        // Save post first to get the post ID
        Posts savedPost = postRepository.save(post);

        // Upload featured image if provided
        if (featuredImage != null && !featuredImage.isEmpty()) {
            String featuredImageUrl = fileService.uploadFeaturedImage(savedPost.getId(), featuredImage);
            savedPost.setFeaturedImageUrl(featuredImageUrl);
            savedPost = postRepository.save(savedPost);
        }

        // Move content files (images, documents, archives) from temp to post folder
        // Extract file URLs from HTML content and move them from temp to post-specific folder
        if (!sanitizedHtml.isEmpty()) {
            List<String> fileUrls = fileService.extractFileUrlsFromContent(sanitizedHtml);
            fileService.moveContentFilesToPost(savedPost.getId(), fileUrls);
        }

        // Add tags
        for (Integer tagId : req.getTagId()) {
            Tags tag = tagsRepository.findById(tagId)
                    .orElseThrow(() -> new IllegalArgumentException("Tag not found with id: " + tagId));

            PostTags postTag = new PostTags();
            postTag.setPost(savedPost);
            postTag.setTag(tag);
            postTagsRepository.save(postTag);
        }

        return postMapper.toSimplePostDTO(savedPost);
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
