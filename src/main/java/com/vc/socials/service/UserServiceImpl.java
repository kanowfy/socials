package com.vc.socials.service;

import com.vc.socials.model.Post;
import com.vc.socials.model.User;
import com.vc.socials.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class UserServiceImpl implements UserService{
    private UserRepository userRepository;
    @Autowired
    public UserServiceImpl(UserRepository userRepository){
        this.userRepository = userRepository;
    }

    @Override
    @Transactional
    public Optional<User> getUserById(Long id) {
        return userRepository.findById(id);
    }

    @Override
    @Transactional
    public Optional<User> getUserByUserName(String username) {
        return userRepository.findByUsername(username);
    }

    @Override
    @Transactional
    public void addPost(Post post, Long Id) {
        User user = userRepository.findById(Id).get();
        List<Post> postList = user.getPosts();
        if (postList == null){
            postList = new ArrayList<>();
        }
        postList.add(post);
        post.setUser(user);
    }
}
