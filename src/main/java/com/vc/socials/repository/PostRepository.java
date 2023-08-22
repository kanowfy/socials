package com.vc.socials.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.vc.socials.model.Post;

public interface PostRepository extends JpaRepository<Post, Long> {

}
