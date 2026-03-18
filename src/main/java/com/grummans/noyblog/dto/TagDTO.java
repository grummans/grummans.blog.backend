package com.grummans.noyblog.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

public class TagDTO {

    private TagDTO() {
        // Utility class - hide implicit public constructor
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Req {
        private String name;
        private String slug;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TagSimpleDTO {
        private int id;
        private String name;
        private String slug;
        private int postCount;
    }
}
