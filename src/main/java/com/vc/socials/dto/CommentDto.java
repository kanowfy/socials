package com.vc.socials.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;

import com.fasterxml.jackson.annotation.JsonProperty;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CommentDto {
    @JsonProperty("comment_id")
    private Long commentId;
    @JsonProperty("post_id")
    private Long postId;
    @JsonProperty("user_id")
    private Long userId;
    private String content;
    @JsonProperty("created_at")
    private Timestamp createdAt;
}
