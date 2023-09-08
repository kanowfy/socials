package com.vc.socials.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CommentDto {
    private Long comment_id;
    private Long post_id;
    private Long user_id;
    private String content;
    private Timestamp created_at;
}
