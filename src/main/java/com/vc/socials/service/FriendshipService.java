package com.vc.socials.service;

import com.vc.socials.model.Friendship;
import com.vc.socials.model.User;

import java.util.List;
import java.util.Optional;

public interface FriendshipService {
    public Optional<Friendship> getFriendshipByID(Long id);
    public Optional<Friendship> getFriendshipByUsersID(Long id1, Long id2);

    public List<Friendship> getFriendshipsByUser1(User user1);

    public List<Friendship> getFriendshipsByUser2(User user2);

    public Boolean checkFriendship(User user1, User user2);

    public Friendship saveFriendship(Friendship friendship);
}
