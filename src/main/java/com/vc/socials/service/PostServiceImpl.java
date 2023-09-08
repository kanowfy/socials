package com.vc.socials.service;

import com.vc.socials.model.Comment;
import com.vc.socials.model.Post;
import com.vc.socials.model.User;
import com.vc.socials.repository.PostRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class PostServiceImpl implements PostService {
    private PostRepository postRepository;

    public PostServiceImpl(PostRepository postRepository) {
        this.postRepository = postRepository;
    }

    @Override
    @Transactional
    public List<Post> getPosts() {
        return postRepository.findPosts();
    }

    @Override
    @Transactional
    public List<Post> getPosts(int limit) {
        Pageable pageable = PageRequest.of(0, limit);
        return postRepository.findPostsWithPagination(pageable);
    }

    @Override
    @Transactional
    public List<Post> getPostsByUserID(User userID, int limit) {
        Pageable pageable = PageRequest.of(0, limit);
        return postRepository.findPostsByUserID(userID, pageable);
    }

    @Override
    @Transactional
    public Long getRowCount() {
        return postRepository.getRowCount();
    }

    @Override
    @Transactional
    public Long getPostCountByUserID(User userID) {
        return postRepository.getPostCountByUserID(userID);
    }

    @Override
    @Transactional
    public Optional<Post> getPostById(Long Id) {
        return postRepository.findById(Id);
    }

    @Override
    @Transactional
    public void savePost(Post post) {
        postRepository.save(post);
    }

    @Override
    @Transactional
    public void addComment(Comment comment, Long postID) {
        Post post = postRepository.findById(postID).get();
        comment.setPost(post);
        post.getComments().add(comment);
    }
}
