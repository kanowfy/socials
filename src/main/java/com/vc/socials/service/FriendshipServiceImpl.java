package com.vc.socials.service;

import com.vc.socials.model.Friendship;
import com.vc.socials.model.FriendshipStatus;
import com.vc.socials.model.User;
import com.vc.socials.repository.FriendshipRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.swing.text.html.Option;
import java.util.List;
import java.util.Optional;

@Service
public class FriendshipServiceImpl implements FriendshipService{
    private FriendshipRepository friendshipRepository;
    @Autowired
    public FriendshipServiceImpl(FriendshipRepository friendshipRepository){
        this.friendshipRepository = friendshipRepository;
    }

    @Override
    @Transactional
    public Optional<Friendship> getFriendshipByID(Long id) {
        return friendshipRepository.findById(id);
    }

    @Override
    @Transactional
    public Optional<Friendship> getFriendshipByUsersID(Long id1, Long id2) {
        return friendshipRepository.findsByUsersId(id1, id2);
    }

    @Override
    @Transactional
    public List<Friendship> getFriendshipsByUser1(User user1) {
        return friendshipRepository.findByUser1(user1);
    }

    @Override
    @Transactional
    public List<Friendship> getFriendshipsByUser2(User user2) {
        return friendshipRepository.findByUser2(user2);
    }

    @Override
    public Boolean checkFriendship(User user1, User user2) {
        Optional<Friendship> f = friendshipRepository.findsByUsersId(user1.getId(), user2.getId());
        if (f.isEmpty()) return false;
        if (f.get().getStatus() == FriendshipStatus.PENDING) return false;
        if (f.get().getStatus() == FriendshipStatus.REJECTED) return false;
        return true;
    }

    @Override
    @Transactional
    public Friendship saveFriendship(Friendship friendship) {
        return friendshipRepository.save(friendship);
    }
}
