package com.vc.socials.repository;

import com.vc.socials.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.vc.socials.model.Post;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface PostRepository extends JpaRepository<Post, Long> {
    @Query("from Post order by createdAt desc")
    public List<Post> findPosts();
    @Query("from Post order by createdAt desc")
    public List<Post> findPostsWithPagination(Pageable pageable);

    @Query("from Post as p where p.user = ?1 order by p.createdAt desc")
    public List<Post> findPostsByUserID(User userID, Pageable pageable);
    @Query("select count(*) from Post ")
    public Long getRowCount();
    @Query("select count(*) from Post as p where p.user = ?1")
    public Long getPostCountByUserID(User userID);
}
