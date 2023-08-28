package com.vc.socials.service;

import com.vc.socials.model.Post;
import com.vc.socials.model.User;
import com.vc.socials.repository.UserRepostitory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class UserServiceImpl implements UserService{
    private UserRepostitory userRepostitory;
    @Autowired
    public UserServiceImpl(UserRepostitory userRepostitory){
        this.userRepostitory = userRepostitory;
    }

    @Override
    @Transactional
    public Optional<User> getUserById(Long id) {
        return userRepostitory.findById(id);
    }

    @Override
    @Transactional
    public Optional<User> getUserByUserName(String username) {
        return userRepostitory.findByUsername(username);
    }

    @Override
    @Transactional
    public void addPost(Post post, Long Id) {
        User user = userRepostitory.findById(Id).get();
        List<Post> postList = user.getPosts();
        if (postList == null){
            postList = new ArrayList<>();
        }
        postList.add(post);
        post.setUser(user);
    }
}
