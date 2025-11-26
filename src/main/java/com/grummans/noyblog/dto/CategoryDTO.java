package com.grummans.noyblog.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

public class CategoryDTO {

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CategorySimpleDTO {
        private int id;
        private String name;
        private String slug;
    }
}
