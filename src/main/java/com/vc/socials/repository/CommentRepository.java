package com.vc.socials.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.vc.socials.model.Comment;

public interface CommentRepository extends JpaRepository<Comment, Long> {

}
