package com.vc.socials.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.vc.socials.model.Friendship;

public interface FriendshipRepository extends JpaRepository<Friendship, Long> {

}
