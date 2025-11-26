package com.grummans.noyblog.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

public class UserDTO {

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AuthorDTO {
        private int id;
        private String username;
        private String displayName;
    }
}
