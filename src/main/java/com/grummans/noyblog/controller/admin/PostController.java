package com.grummans.noyblog.controller.admin;

import com.grummans.noyblog.configuration.ApiResponse;
import com.grummans.noyblog.configuration.PageResponse;
import com.grummans.noyblog.dto.PostDTO;
import com.grummans.noyblog.services.admin.PostService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/a/posts")
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;

    @GetMapping("/list")
    @CrossOrigin
    public ApiResponse<PageResponse<PostDTO.Res>> getAllPosts(
            @RequestParam(required = false) String title,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        ApiResponse<PageResponse<PostDTO.Res>> response = new ApiResponse<>();
        int pageNumber = page > 0 ? page - 1 : 0;

        PostDTO.Req req = new PostDTO.Req();
        req.setTitle(title);

        Page<PostDTO.Res> postsPage = postService.getAllPost(req, pageNumber, size);

        response.setCode(200);
        response.setData(new PageResponse<>(postsPage));
        return response;
    }

    @CrossOrigin
    @PostMapping("/create")
    public ApiResponse<PostDTO.Res> createPost(@RequestBody PostDTO.Req req) {
        ApiResponse<PostDTO.Res> response = new ApiResponse<>();
        response.setCode(201);
        response.setData(postService.createPost(req));
        return response;
    }
}
