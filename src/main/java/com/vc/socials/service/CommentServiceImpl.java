package com.vc.socials.service;

import com.vc.socials.model.Comment;
import com.vc.socials.repository.CommentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class CommentServiceImpl implements CommentService {
    private CommentRepository commentRepository;

    public CommentServiceImpl(CommentRepository commentRepository) {
        this.commentRepository = commentRepository;
    }

    @Override
    @Transactional
    public List<Comment> getComments() {
        return commentRepository.findComments();
    }

    @Override
    @Transactional
    public Optional<Comment> getCommentById(Long Id) {
        return commentRepository.findById(Id);
    }

    @Override
    @Transactional
    public Long saveComment(Comment comment) {
        commentRepository.saveAndFlush(comment);
        return comment.getId();
    }
}
