package com.grummans.noyblog.mapper;

import com.grummans.noyblog.dto.PostDTO;
import com.grummans.noyblog.model.Posts;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface PostMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "contentHtml", ignore = true)
    @Mapping(target = "isFeatured", ignore = true)
    @Mapping(target = "viewCount", ignore = true)
    @Mapping(target = "publishedAt", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Posts toPost(PostDTO.Req postDTO);

    @Mapping(target = "tagId", ignore = true)
    PostDTO.Res toPostDTO(Posts post);
}
