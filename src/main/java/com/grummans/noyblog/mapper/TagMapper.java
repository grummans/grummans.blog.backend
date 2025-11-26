package com.grummans.noyblog.mapper;

import com.grummans.noyblog.dto.TagDTO;
import com.grummans.noyblog.model.Tags;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface TagMapper {

    TagDTO.TagSimpleDTO toTagSimpleDTO(Tags tag);
}
