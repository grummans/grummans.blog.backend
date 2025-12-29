package com.grummans.noyblog.mapper;

import com.grummans.noyblog.dto.UserDTO;
import com.grummans.noyblog.model.Users;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface UserMapper {

    UserDTO.AuthorDTO toAuthorDTO(Users user);

}
