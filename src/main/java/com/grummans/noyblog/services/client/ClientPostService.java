package com.grummans.noyblog.services.client;

import com.grummans.noyblog.dto.PostDTO;
import com.grummans.noyblog.mapper.CategoryMapper;
import com.grummans.noyblog.mapper.PostMapper;
import com.grummans.noyblog.mapper.TagMapper;
import com.grummans.noyblog.mapper.UserMapper;
import com.grummans.noyblog.model.*;
import com.grummans.noyblog.repository.CategoryRepository;
import com.grummans.noyblog.repository.PostRepository;
import com.grummans.noyblog.repository.PostTagsRepository;
import com.grummans.noyblog.repository.UsersRepository;
import com.grummans.noyblog.services.system.FileService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ClientPostService {

    private static final String STATUS_PUBLISHED = "PUBLISHED";
    private static final String ERROR_CATEGORY_NOT_FOUND = "Category not found with id: ";
    private static final String ERROR_POST_NOT_FOUND = "Post not found with id: ";
    private static final String ERROR_AUTHOR_NOT_FOUND = "Author not found with id: ";

    private final FileService fileService;
    private final PostRepository postRepository;
    private final UsersRepository usersRepository;
    private final CategoryRepository categoryRepository;
    private final PostTagsRepository postTagsRepository;
    private final CategoryMapper categoryMapper;
    private final UserMapper userMapper;
    private final PostMapper postMapper;
    private final TagMapper tagMapper;

    public List<PostDTO.PostForClientDTO> getAllPosts() {
        return postRepository.findAllByStatus(STATUS_PUBLISHED).stream()
                .map(this::mapToPostForClientDTO)
                .toList();
    }

    public List<PostDTO.PostForClientDTO> getFeaturedPosts() {
        return postRepository.findByStatusAndIsFeatured(STATUS_PUBLISHED, true).stream()
                .map(this::mapToPostForClientDTO)
                .toList();
    }

    public PostDTO.Res getDetailPost(int postId) {
        Posts post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException(ERROR_POST_NOT_FOUND + postId));

        return mapToPostRes(post, postId);
    }

    public PostDTO.Res getDetailPostBySlug(String slug) {
        Posts post = postRepository.findBySlug(slug);
        if (post == null) {
            throw new IllegalArgumentException("Post not found with slug: " + slug);
        }

        return mapToPostRes(post, post.getId());
    }

    // ==================== HELPER METHODS ====================

    /**
     * Map post entity to PostForClientDTO with category and tags
     */
    private PostDTO.PostForClientDTO mapToPostForClientDTO(Posts post) {
        PostDTO.PostForClientDTO postDTO = postMapper.toPostDTOForClient(post);

        Categories category = categoryRepository.findById(post.getCategoryId())
                .orElseThrow(() -> new IllegalArgumentException(ERROR_CATEGORY_NOT_FOUND + post.getCategoryId()));
        postDTO.setCategory(categoryMapper.toCategorySimpleDTO(category));

        List<Tags> tags = postTagsRepository.findByPostId(post.getId());
        postDTO.setTags(tags.stream()
                .map(tagMapper::toTagSimpleDTO)
                .toList());

        return postDTO;
    }

    /**
     * Map post entity to PostDTO.Res with author, category, tags and attachments
     */
    private PostDTO.Res mapToPostRes(Posts post, int postId) {
        PostDTO.Res postDTO = postMapper.toPostDTO(post);

        Users author = usersRepository.findById(post.getAuthorId())
                .orElseThrow(() -> new IllegalArgumentException(ERROR_AUTHOR_NOT_FOUND + post.getAuthorId()));
        postDTO.setAuthor(userMapper.toAuthorDTO(author));

        Categories category = categoryRepository.findById(post.getCategoryId())
                .orElseThrow(() -> new IllegalArgumentException(ERROR_CATEGORY_NOT_FOUND + post.getCategoryId()));
        postDTO.setCategory(categoryMapper.toCategorySimpleDTO(category));

        List<Tags> tags = postTagsRepository.findByPostId(post.getId());
        postDTO.setTags(tags.stream()
                .map(tagMapper::toTagSimpleDTO)
                .toList());

        postDTO.setAttachments(fileService.getPostAttachments(postId));

        return postDTO;
    }
}
