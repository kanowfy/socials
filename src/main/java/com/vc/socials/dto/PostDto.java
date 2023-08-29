package com.vc.socials.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PostDto {
        private Long post_id;
        private Long user_id;
        private String content;
        private List<CommentDto> commentDtoList;
        private Timestamp created_at;
}
