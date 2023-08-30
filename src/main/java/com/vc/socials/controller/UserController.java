package com.vc.socials.controller;

import com.vc.socials.dto.SignUpDto;
import com.vc.socials.model.User;
import com.vc.socials.repository.UserRepostitory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@RestController
public class UserController {
    private UserRepostitory userRepostitory;
    private BCryptPasswordEncoder passwordEncoder;

    public UserController(UserRepostitory userRepostitory, BCryptPasswordEncoder passwordEncoder) {
        this.userRepostitory = userRepostitory;
        this.passwordEncoder = passwordEncoder;
    }

    @PostMapping("/api/users/register")
    public ResponseEntity<?> registerUser(@RequestBody SignUpDto signUpDto) {
        // add check for username exists in a DB
        if (userRepostitory.existsByUsername(signUpDto.getUsername())) {
            return new ResponseEntity<>("Username is already taken!",
                    HttpStatus.BAD_REQUEST);
        }

        // add check for email exists in DB
        if (userRepostitory.existsByEmail(signUpDto.getEmail())) {
            return new ResponseEntity<>("Email is already taken!",
                    HttpStatus.BAD_REQUEST);
        }

        // create user object
        User user = new User();
        user.setFullname(signUpDto.getFullname());
        user.setUsername(signUpDto.getUsername());
        user.setEmail(signUpDto.getEmail());
        user.setPassword(passwordEncoder.encode(signUpDto.getPassword()));

        userRepostitory.save(user);

        return new ResponseEntity<>("User registered successfully", HttpStatus.OK);

    }
}
