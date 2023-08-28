package com.vc.socials.dto;

import lombok.Data;

@Data
public class FriendRequestDto {
    private Long fromUserId;
    private Long toUserId;
}
