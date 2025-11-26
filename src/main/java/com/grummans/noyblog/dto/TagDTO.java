package com.grummans.noyblog.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

public class TagDTO {

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TagSimpleDTO {
        private int id;
        private String name;
        private String slug;
    }
}
