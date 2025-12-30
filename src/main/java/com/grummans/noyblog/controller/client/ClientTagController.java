package com.grummans.noyblog.controller.client;

import com.grummans.noyblog.configuration.ApiResponse;
import com.grummans.noyblog.dto.TagDTO;
import com.grummans.noyblog.services.client.ClientTagService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/c/tags")
public class ClientTagController {

    private final ClientTagService clientTagService;

    @GetMapping
    public ApiResponse<List<TagDTO.TagSimpleDTO>> getAllTags() {
        ApiResponse<List<TagDTO.TagSimpleDTO>> response = new ApiResponse<>();
        response.setCode(200);
        response.setMessage("success");
        response.setData(clientTagService.getAllTags());
        return response;
    }
}
