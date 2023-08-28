package com.vc.socials.service;

import com.vc.socials.model.Comment;

import java.util.List;
import java.util.Optional;

public interface CommentService {
    public List<Comment> getComments();

    public Optional<Comment> getCommentById(Long Id);

    public Long saveComment(Comment comment);
}
