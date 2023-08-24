package com.vc.socials.controller;

import com.vc.socials.dto.LoginDto;
import com.vc.socials.dto.SignUpDto;
import com.vc.socials.model.User;
import com.vc.socials.repository.UserRepostitory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/")
public class UserController {
    private UserRepostitory userRepostitory;
    @Autowired
    public UserController(UserRepostitory userRepostitory){
        this.userRepostitory = userRepostitory;
    }
    @PostMapping("/api/users/register")
    public ResponseEntity<?> registerUser(@RequestBody SignUpDto signUpDto){

        // add check for username exists in a DB
        if(userRepostitory.existsByUsername(signUpDto.getUsername())){
            return new ResponseEntity<>("Username is already taken!", HttpStatus.BAD_REQUEST);
        }

        // add check for email exists in DB
        if(userRepostitory.existsByEmail(signUpDto.getEmail())){
            return new ResponseEntity<>("Email is already taken!", HttpStatus.BAD_REQUEST);
        }

        // create user object
        User user = new User();
        user.setUsername(signUpDto.getUsername());
        user.setEmail(signUpDto.getEmail());
        user.setPassword(signUpDto.getPassword());


        userRepostitory.save(user);

        return new ResponseEntity<>("User registered successfully", HttpStatus.OK);
    }
    @PostMapping("/api/users/login")
    public ResponseEntity<String> authenticateUser(@RequestBody LoginDto loginDto){
        if (userRepostitory.existsByUsernameAndPassword(loginDto.getUsername(), loginDto.getPassword()))
        return new ResponseEntity<>("User signed-in successfully!.", HttpStatus.OK);
        else return new ResponseEntity<>("Login Failed!", HttpStatus.OK);
    }
}
