package com.vc.socials.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PostDto {
        @JsonProperty("postId")
        private Long postId;
        @JsonProperty("userId")
        private Long userId;
        private String content;
        private List<CommentDto> comments;
        @JsonProperty("created_at")
        private Timestamp createdAt;
}
