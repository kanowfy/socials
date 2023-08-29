package com.vc.socials.dto;

import com.vc.socials.model.User;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;
import java.util.List;

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
