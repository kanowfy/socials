package com.vc.socials.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.vc.socials.model.Friendship;
import com.vc.socials.model.User;

import java.util.List;

public interface FriendshipRepository extends JpaRepository<Friendship, Long> {
    @Query("SELECT f FROM Friendship f JOIN f.user1 u1 JOIN f.user2 u2 WHERE (u1.id = :id1 AND u2.id = :id2) OR (u1.id = :id2 AND u2.id = :id1)")
    Optional<Friendship> findsByUsersId(@Param("id1") Long id1, @Param("id2") Long id2);

    List<Friendship> findByUser1(User user1);

    List<Friendship> findByUser2(User user2);
}
