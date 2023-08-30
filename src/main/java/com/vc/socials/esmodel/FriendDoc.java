package com.vc.socials.esmodel;

import lombok.Data;

@Data
public class FriendDoc {
    private Long user1Id;

    private Long user2Id;

    private String user1Fullname;

    private String user2Fullname;
}
