package com.grummans.noyblog.mapper;

import com.grummans.noyblog.dto.PostDTO;
import com.grummans.noyblog.model.Posts;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface PostMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "isFeatured", ignore = true)
    @Mapping(target = "viewCount", ignore = true)
    @Mapping(target = "publishedAt", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Posts toPost(PostDTO.Req postDTO);

    // Map Posts entity to Response DTO
    // Note: content (markdown) is ignored by default, only contentHtml is mapped for display
    @Mapping(target = "content", ignore = true) // Don't map markdown content to response
    // MapStruct resolves boolean property name as "featured" (getter is isFeatured / setFeatured)
    @Mapping(source = "featured", target = "featured")
    PostDTO.Res toPostDTO(Posts post);

    // Explicit mapping for Simple DTO is not necessary but keep default
    PostDTO.SimplePostDTO toSimplePostDTO(Posts post);
}
