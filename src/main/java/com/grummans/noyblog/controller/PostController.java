package com.grummans.noyblog.controller;

import com.grummans.noyblog.configuration.PageResponse;
import com.grummans.noyblog.dto.PostDTO;
import com.grummans.noyblog.services.PostService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/a/posts")
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;

    @GetMapping("/list")
    public ResponseEntity<PageResponse<PostDTO.Res>> getAllPosts(
            @RequestParam(required = false) String title,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        int pageNumber = page > 0 ? page - 1 : 0;

        PostDTO.Req req = new PostDTO.Req();
        req.setTitle(title);

        Page<PostDTO.Res> postsPage = postService.getAllPost(req, pageNumber, size);

        return ResponseEntity.ok(new PageResponse<>(postsPage));
    }

    @PostMapping("/create")
    public ResponseEntity<Integer> createPost(@RequestBody PostDTO.Req req) {
        int postId = postService.createPost(req);
        return ResponseEntity.ok(postId);
    }
}
