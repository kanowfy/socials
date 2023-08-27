package com.vc.socials.service;

import java.util.ArrayList;
import java.util.Optional;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.vc.socials.model.User;
import com.vc.socials.repository.UserRepostitory;

@Service
public class CustomUserDetailsService implements UserDetailsService {
    private UserRepostitory userRepostitory;

    public CustomUserDetailsService(UserRepostitory userRepostitory) {
        this.userRepostitory = userRepostitory;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Optional<User> user = userRepostitory.findByUsername(username);

        if (!user.isPresent()) {
            throw new UsernameNotFoundException("User not found");
        }

        return new org.springframework.security.core.userdetails.User(user.get().getUsername(),
                user.get().getPassword(), new ArrayList<>());
    }

}
