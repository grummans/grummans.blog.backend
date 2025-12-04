package com.grummans.noyblog.controller.client;

import com.grummans.noyblog.configuration.ApiResponse;
import com.grummans.noyblog.dto.PostDTO;
import com.grummans.noyblog.services.client.ClientPostService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/c/posts")
@RequiredArgsConstructor
public class ClientPostController {

    private final ClientPostService postService;

    @CrossOrigin
    @GetMapping
    public ApiResponse<List<PostDTO.PostForClientDTO>> getAllPosts() {
        ApiResponse<List<PostDTO.PostForClientDTO>> response = new ApiResponse<>();
        List<PostDTO.PostForClientDTO> posts = postService.getAllPosts();
        response.setCode(200);
        response.setData(posts);
        return response;
    }

    @CrossOrigin
    @GetMapping("/featured")
    public ApiResponse<List<PostDTO.PostForClientDTO>> getFeaturedPosts() {
        ApiResponse<List<PostDTO.PostForClientDTO>> response = new ApiResponse<>();
        List<PostDTO.PostForClientDTO> posts = postService.getFeaturedPosts();
        response.setCode(200);
        response.setData(posts);
        return response;
    }

    @CrossOrigin
    @GetMapping("/{postId}")
    public ApiResponse<PostDTO.Res> getDetailPost(@PathVariable int postId) {
        ApiResponse<PostDTO.Res> response = new ApiResponse<>();
        PostDTO.Res post = postService.getDetailPost(postId);
        response.setCode(200);
        response.setData(post);
        return response;
    }

    @CrossOrigin
    @GetMapping("/slug/{slug}")
    public ApiResponse<PostDTO.Res> getDetailPostBySlug(@PathVariable String slug) {
        ApiResponse<PostDTO.Res> response = new ApiResponse<>();
        PostDTO.Res post = postService.getDetailPostBySlug(slug);
        response.setCode(200);
        response.setData(post);
        return response;
    }
}
