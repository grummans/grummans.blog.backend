package com.grummans.noyblog.controller.admin;

import com.grummans.noyblog.configuration.ApiResponse;
import com.grummans.noyblog.dto.TagDTO;
import com.grummans.noyblog.services.admin.TagService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/a/tags")
@RequiredArgsConstructor
public class TagController {

    private final TagService tagService;

    @CrossOrigin
    @GetMapping()
    public ApiResponse<List<TagDTO.TagSimpleDTO>> getAllTags() {
        ApiResponse<List<TagDTO.TagSimpleDTO>> response = new ApiResponse<>();
        response.setCode(200);
        response.setData(tagService.getAllTags());
        response.setMessage("success");
        return response;
    }

    @CrossOrigin
    @PostMapping("/create")
    public ApiResponse<TagDTO.TagSimpleDTO> createTag(@RequestBody TagDTO.Req req) {
        ApiResponse<TagDTO.TagSimpleDTO> response = new ApiResponse<>();
        response.setCode(200);
        response.setData(tagService.createTag(req));
        response.setMessage("Tag created successfully");
        return response;
    }

    @CrossOrigin
    @GetMapping("/{tagId}")
    public ApiResponse<TagDTO.TagSimpleDTO> getTag(@PathVariable Integer tagId) {
        ApiResponse<TagDTO.TagSimpleDTO> response = new ApiResponse<>();
        response.setCode(200);
        response.setData(tagService.getDetailTag(tagId));
        response.setMessage("success");
        return response;
    }

    @CrossOrigin
    @PutMapping("/{tagId}/update")
    public ApiResponse<TagDTO.TagSimpleDTO> updateTag(@PathVariable int tagId, @RequestBody TagDTO.Req req) {
        ApiResponse<TagDTO.TagSimpleDTO> response = new ApiResponse<>();
        response.setCode(200);
        response.setData(tagService.updateTag(tagId, req));
        response.setMessage("Tag updated successfully");
        return response;
    }

    @CrossOrigin
    @DeleteMapping("/{tagId}")
    public ApiResponse<Void> deleteTag(@PathVariable int tagId) {
        ApiResponse<Void> response = new ApiResponse<>();
        tagService.deleteTag(tagId);
        response.setCode(200);
        response.setMessage("Tag deleted successfully");
        return response;
    }
}
