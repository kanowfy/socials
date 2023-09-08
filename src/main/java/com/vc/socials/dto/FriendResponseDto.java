package com.vc.socials.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
public class FriendResponseDto {
    @JsonProperty("friendship_id")
    private Long friendshipId;
    private String response;
}
