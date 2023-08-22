package com.vc.socials.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.vc.socials.model.User;

public interface UserRepostitory extends JpaRepository<User, Long> {

}
