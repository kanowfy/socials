package com.vc.socials.service;

import com.vc.socials.model.Comment;
import com.vc.socials.model.Post;
import com.vc.socials.model.User;

import java.util.List;
import java.util.Optional;

public interface PostService {
    public List<Post> getPosts();

    public List<Post> getPosts(int limit);

    public List<Post> getPostsByUserID(User userID, int limit);
    public Long getRowCount();

    public Long getPostCountByUserID(User userID);

    public Optional<Post> getPostById(Long Id);

    public void savePost(Post post);

    public void addComment(Comment comment, Long postID);
}
