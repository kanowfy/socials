package com.vc.socials.service;

import java.io.IOException;
import java.util.List;

import com.vc.socials.esmodel.FriendDoc;
import com.vc.socials.model.User;

public interface FriendSearchService {
    List<User> searchForFriends(Long currentUserId, String term) throws IOException;

    void createFriendIndex(FriendDoc friend) throws IOException;
}
