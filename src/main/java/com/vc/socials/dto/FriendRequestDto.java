package com.vc.socials.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
public class FriendRequestDto {
    @JsonProperty("to_user_id")
    private Long toUserId;
}
