package com.grummans.noyblog.services.client;

import com.grummans.noyblog.dto.CategoryDTO;
import com.grummans.noyblog.dto.PostDTO;
import com.grummans.noyblog.dto.UserDTO;
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
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ClientPostService {

    private final FileService fileService;
    private final PostRepository postRepository;
    private final UsersRepository usersRepository;
    private final CategoryRepository categoryRepository;
    private final PostTagsRepository postTagsRepository;
    private final CategoryMapper categoryMapper;
    private final UserMapper userMapper;
    private final PostMapper postMapper;
    private final TagMapper tagMapper;

    public List<PostDTO.PostForClientDTO> getAllPosts(){
        return postRepository.findAllByStatus("PUBLISHED").stream()
                .map(post -> {
                    PostDTO.PostForClientDTO postDTO = postMapper.toPostDTOForClient(post);

                    Categories category = categoryRepository.findById(post.getCategoryId())
                            .orElseThrow(() -> new IllegalArgumentException("Category not found with id: " + post.getCategoryId()));
                    CategoryDTO.CategorySimpleDTO categoryDTO = categoryMapper.toCategorySimpleDTO(category);
                    postDTO.setCategory(categoryDTO);

                    List<Tags> tags = postTagsRepository.findByPostId(post.getId());
                    postDTO.setTags(tags.stream()
                            .map(tagMapper::toTagSimpleDTO)
                            .collect(Collectors.toList()));

                    return postDTO;
                })
                .toList();
    }

    public List<PostDTO.PostForClientDTO> getFeaturedPosts(){
        return postRepository.findByStatusAndIsFeatured("PUBLISHED", true).stream()
                .map(post -> {
                    PostDTO.PostForClientDTO postDTO = postMapper.toPostDTOForClient(post);

                    Categories category = categoryRepository.findById(post.getCategoryId())
                            .orElseThrow(() -> new IllegalArgumentException("Category not found with id: " + post.getCategoryId()));
                    CategoryDTO.CategorySimpleDTO categoryDTO = categoryMapper.toCategorySimpleDTO(category);
                    postDTO.setCategory(categoryDTO);

                    List<Tags> tags = postTagsRepository.findByPostId(post.getId());
                    postDTO.setTags(tags.stream()
                            .map(tagMapper::toTagSimpleDTO)
                            .collect(Collectors.toList()));

                    return postDTO;
                })
                .toList();
    }

    public PostDTO.Res getDetailPost(int postId){

        Posts post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post not found with id: " + postId));

        PostDTO.Res postDTO = postMapper.toPostDTO(post);

        Users author = usersRepository.findById(post.getAuthorId())
                .orElseThrow(() -> new IllegalArgumentException("Author not found with id: " + post.getAuthorId()));
        UserDTO.AuthorDTO authorDTO = userMapper.toAuthorDTO(author);
        postDTO.setAuthor(authorDTO);

        Categories category = categoryRepository.findById(post.getCategoryId())
                .orElseThrow(() -> new IllegalArgumentException("Category not found with id: " + post.getCategoryId()));
        CategoryDTO.CategorySimpleDTO categoryDTO = categoryMapper.toCategorySimpleDTO(category);
        postDTO.setCategory(categoryDTO);

        List<Tags> tags = postTagsRepository.findByPostId(post.getId());
        postDTO.setTags(tags.stream()
                .map(tagMapper::toTagSimpleDTO)
                .collect(Collectors.toList()));

        List<PostAttachments> attachments = fileService.getPostAttachments(postId);
        postDTO.setAttachments(attachments);

        return postDTO;
    }

    public PostDTO.Res getDetailPostBySlug(String slug){

        Posts post = postRepository.findBySlug(slug);
        if (post == null) {
            throw new RuntimeException("Post not found with slug: " + slug);
        }

        PostDTO.Res postDTO = postMapper.toPostDTO(post);

        Users author = usersRepository.findById(post.getAuthorId())
                .orElseThrow(() -> new IllegalArgumentException("Author not found with id: " + post.getAuthorId()));
        UserDTO.AuthorDTO authorDTO = userMapper.toAuthorDTO(author);
        postDTO.setAuthor(authorDTO);

        Categories category = categoryRepository.findById(post.getCategoryId())
                .orElseThrow(() -> new IllegalArgumentException("Category not found with id: " + post.getCategoryId()));
        CategoryDTO.CategorySimpleDTO categoryDTO = categoryMapper.toCategorySimpleDTO(category);
        postDTO.setCategory(categoryDTO);

        List<Tags> tags = postTagsRepository.findByPostId(post.getId());
        postDTO.setTags(tags.stream()
                .map(tagMapper::toTagSimpleDTO)
                .collect(Collectors.toList()));

        List<PostAttachments> attachments = fileService.getPostAttachments(post.getId());
        postDTO.setAttachments(attachments);

        return postDTO;
    }
}
