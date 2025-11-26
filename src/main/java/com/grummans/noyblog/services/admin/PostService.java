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
import io.minio.MinioClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    public PostDTO.Res createPost(PostDTO.Req req) {
        Posts post = postMapper.toPost(req);
        int authorId = usersRepository.findIdByUsername(req.getAuthorUsername());
        post.setAuthorId(authorId);
        Posts savedPost = postRepository.save(post);
        for (Integer tagId : req.getTagId()) {

            Tags tag = tagsRepository.findById(tagId).orElseThrow(() -> new IllegalArgumentException("Tag not found with id: " + tagId));

            PostTags postTag = new PostTags();
            postTag.setPost(savedPost);
            postTag.setTag(tag);
            postTagsRepository.save(postTag);
        }
        PostDTO.Res postDTORes = postMapper.toPostDTO(savedPost);
//        postDTORes.setTagId(req.getTagId());
        return postDTORes;
    }

}
