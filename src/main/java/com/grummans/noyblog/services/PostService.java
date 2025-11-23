package com.grummans.noyblog.services;

import com.grummans.noyblog.dto.PostDTO;
import com.grummans.noyblog.mapper.PostMapper;
import com.grummans.noyblog.model.PostTags;
import com.grummans.noyblog.model.Posts;
import com.grummans.noyblog.model.Tags;
import com.grummans.noyblog.repository.PostRepository;
import com.grummans.noyblog.repository.PostTagsRepository;
import com.grummans.noyblog.repository.TagsRepository;
import com.grummans.noyblog.repository.UsersRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PostService {

    private final PostRepository postRepository;

    private final PostTagsRepository postTagsRepository;

    private final UsersRepository usersRepository;

    private final TagsRepository tagsRepository;

    private final PostMapper postMapper;

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

        if (req.getTitle() != null && ! req.getTitle().isEmpty()) {
            return postRepository.findByTitleContainingIgnoreCase(req.getTitle(), pageable).map(postMapper::toPostDTO);
        } else {
            return postRepository.findAll(pageable).map(postMapper::toPostDTO);
        }
    }

    @Transactional
    public int createPost(PostDTO.Req req) {
        Posts post = postMapper.toPost(req);
        int authorId = usersRepository.findIdByUsername(req.getAuthorUsername());
        post.setAuthorId(authorId);
        Posts savedPost = postRepository.save(post);
        int postId = savedPost.getId();
        for (Integer tagId : req.getTagId()) {

            Tags tag = tagsRepository.findById(tagId)
                    .orElseThrow(() -> new IllegalArgumentException("Tag not found with id: " + tagId));

            PostTags postTag = new PostTags();
            postTag.setPost(savedPost);
            postTag.setTag(tag);
            postTagsRepository.save(postTag);
        }
        return postId;
    }

}
