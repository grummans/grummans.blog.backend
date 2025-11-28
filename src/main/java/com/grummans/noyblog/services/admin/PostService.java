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
     * Get all posts with optional title filtering and pagination.
     *
     * @param req:  PostDTO.Req object containing filter criteria
     * @param page: page number (0-based)
     * @param size: number of items per page
     * @return Page of PostDTO.Res objects
     */
    public Page<PostDTO.Res> getAllPost(PostDTO.Req req, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());

        Page<Posts> postsList;
        if (req.getTitle() != null && !req.getTitle().isEmpty()) {
            postsList = postRepository.findByTitleContainingIgnoreCase(req.getTitle(), pageable);
        } else {
            postsList = postRepository.findAll(pageable);
        }

        return postsList.map(post -> {

            PostDTO.Res postDTORes = postMapper.toPostDTO(post);

            Users author = usersRepository.findById(post.getAuthorId()).orElseThrow(() -> new IllegalArgumentException("Author not found with id: " + post.getAuthorId()));
            UserDTO.AuthorDTO authorDTO = userMapper.toAuthorDTO(author);

            postDTORes.setAuthor(authorDTO);

            Categories category = categoryRepository.findById(post.getCategoryId()).orElseThrow(() -> new IllegalArgumentException("Category not found with id: " + post.getCategoryId()));

            CategoryDTO.CategorySimpleDTO categoryDTO = categoryMapper.toCategorySimpleDTO(category);

            postDTORes.setCategory(categoryDTO);

            List<Tags> tags = postTagsRepository.findByPostId(post.getId());

            postDTORes.setTags(tags.stream().map(tagMapper::toTagSimpleDTO).collect(Collectors.toList()));

            return postDTORes;
        });
    }

    @Transactional
    public PostDTO.SimplePostDTO createPost(PostDTO.Req req, MultipartFile featuredImage) {
        // Process content: FE sends HTML in 'content' field, we need to convert and save properly
        ContentService.ContentData contentData = contentService.processContent(req.getContent());

        // Create post entity
        Posts post = postMapper.toPost(req);

        // Override content fields with processed data
        post.setContent(contentData.getMarkdown());      // Markdown for editing
        post.setContentHtml(contentData.getHtml());      // HTML for display

        int authorId = usersRepository.findIdByUsername(req.getAuthorUsername());
        post.setAuthorId(authorId);

        // Save post first to get the post ID
        Posts savedPost = postRepository.save(post);

        // Upload featured image if provided
        if (featuredImage != null && !featuredImage.isEmpty()) {
            String featuredImageUrl = fileService.uploadFeaturedImage(savedPost.getId(), featuredImage);
            savedPost.setFeaturedImageUrl(featuredImageUrl);
            savedPost = postRepository.save(savedPost);
        }

        // Move content files (images, documents, archives) from temp to post folder
        // Process HTML content from TipTap Editor
        String htmlContent = contentData.getHtml();
        if (htmlContent != null && !htmlContent.isEmpty()) {
            List<String> fileUrls = fileService.extractFileUrlsFromContent(htmlContent);
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

    public PostDTO.Res detailPost(int postId) {
        return getPostDetail(postId, false);
    }

    /**
     * Get post detail for editing (returns HTML for TipTap Editor)
     */
    public PostDTO.Res detailPostForEdit(int postId) {
        return getPostDetail(postId, true);
    }

    private PostDTO.Res getPostDetail(int postId, boolean forEdit) {
        Posts post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("Post not found with id: " + postId));

        PostDTO.Res postDTORes = postMapper.toPostDTO(post);

        // For editing, return HTML content (TipTap needs HTML)
        // For display, return HTML content for direct rendering
        if (forEdit) {
            // Return HTML for TipTap Editor
            postDTORes.setContent(post.getContentHtml() != null ? post.getContentHtml() :
                                 (post.getContent() != null ? post.getContent() : ""));
        }
        // For display, mapper already handles contentHtml field

        Users author = usersRepository.findById(post.getAuthorId())
                .orElseThrow(() -> new IllegalArgumentException("Author not found with id: " + post.getAuthorId()));
        UserDTO.AuthorDTO authorDTO = userMapper.toAuthorDTO(author);
        postDTORes.setAuthor(authorDTO);

        Categories category = categoryRepository.findById(post.getCategoryId())
                .orElseThrow(() -> new IllegalArgumentException("Category not found with id: " + post.getCategoryId()));
        CategoryDTO.CategorySimpleDTO categoryDTO = categoryMapper.toCategorySimpleDTO(category);
        postDTORes.setCategory(categoryDTO);

        List<Tags> tags = postTagsRepository.findByPostId(post.getId());
        postDTORes.setTags(tags.stream()
                .map(tagMapper::toTagSimpleDTO)
                .collect(Collectors.toList()));

        return postDTORes;
    }
}
