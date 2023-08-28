package com.vc.socials.service;

import com.vc.socials.model.Post;
import com.vc.socials.model.User;

import java.util.Optional;

public interface UserService {
    public Optional<User> getUserById(Long id);

    public Optional<User> getUserByUserName(String username);

    public void addPost(Post post, Long Id);

}
