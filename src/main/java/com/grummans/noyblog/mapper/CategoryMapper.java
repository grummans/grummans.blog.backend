package com.grummans.noyblog.mapper;

import com.grummans.noyblog.dto.CategoryDTO;
import com.grummans.noyblog.model.Categories;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring", unmappedTargetPolicy = org.mapstruct.ReportingPolicy.IGNORE)
public interface CategoryMapper {

    CategoryDTO.CategorySimpleDTO toCategorySimpleDTO(Categories category);

}
