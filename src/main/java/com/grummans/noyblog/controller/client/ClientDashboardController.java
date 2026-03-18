package com.grummans.noyblog.controller.client;

import com.grummans.noyblog.configuration.ApiResponse;
import com.grummans.noyblog.dto.PostDTO;
import com.grummans.noyblog.services.client.ClientPostService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/c/dashboard")
@RequiredArgsConstructor
public class ClientDashboardController {

    private final ClientPostService clientPostService;

    @GetMapping
    public ApiResponse<List<PostDTO.DashboardPostDTO>> getDashboardPosts() {
        ApiResponse<List<PostDTO.DashboardPostDTO>> response = new ApiResponse<>();
        List<PostDTO.DashboardPostDTO> posts = clientPostService.getLatestPostsForDashboard();
        response.setCode(200);
        response.setData(posts);
        return response;
    }
}
