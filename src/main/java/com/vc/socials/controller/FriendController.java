package com.vc.socials.controller;

import java.io.IOException;
import java.security.Principal;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.vc.socials.dto.FriendRequestDto;
import com.vc.socials.dto.FriendResponseDto;
import com.vc.socials.dto.FriendSearchDto;
import com.vc.socials.dto.UserDto;
import com.vc.socials.esmodel.FriendDoc;
import com.vc.socials.model.Friendship;
import com.vc.socials.model.FriendshipStatus;
import com.vc.socials.model.User;
import com.vc.socials.repository.FriendshipRepository;
import com.vc.socials.repository.UserRepostitory;
import com.vc.socials.service.FriendSearchService;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@RestController
public class FriendController {
    private UserRepostitory userRepostitory;
    private FriendshipRepository friendshipRepository;
    private FriendSearchService friendSearchService;

    public FriendController(UserRepostitory userRepostitory, FriendshipRepository friendshipRepository,
            FriendSearchService friendSearchService) {
        this.userRepostitory = userRepostitory;
        this.friendshipRepository = friendshipRepository;
        this.friendSearchService = friendSearchService;
    }

    @GetMapping("/api/friends/hello")
    public ResponseEntity<String> sayHello(Principal principal) {
        return new ResponseEntity<String>(principal.getName(), HttpStatus.OK);
    }

    @PostMapping("/api/friends/request")
    public ResponseEntity<?> makeFriendRequest(@RequestBody FriendRequestDto friendRequestDto) {
        Optional<User> user1 = userRepostitory.findById(friendRequestDto.getFromUserId());
        Optional<User> user2 = userRepostitory.findById(friendRequestDto.getToUserId());

        if (!user1.isPresent()) {
            return new ResponseEntity<String>(
                    "User with id %d does not exist.".formatted(friendRequestDto.getFromUserId()),
                    HttpStatus.BAD_REQUEST);
        }

        if (!user2.isPresent()) {
            return new ResponseEntity<String>(
                    "User with id %d does not exist.".formatted(friendRequestDto.getToUserId()),
                    HttpStatus.BAD_REQUEST);
        }

        // check if friendship record exists between the 2 users
        if (friendshipRepository.findsByUsersId(friendRequestDto.getFromUserId(), friendRequestDto.getToUserId())
                .isPresent()) {
            return new ResponseEntity<String>("Friendship record already exists.", HttpStatus.BAD_REQUEST);
        }

        Friendship friendship = new Friendship();
        friendship.setUser1(user1.get());
        friendship.setUser2(user2.get());
        friendship.setStatus(FriendshipStatus.PENDING);
        friendship.setCreatedAt(Timestamp.from(Instant.now()));
        Friendship savedFriendship = friendshipRepository.save(friendship);

        // make request notification here

        return new ResponseEntity<String>("Friend request created with id %d.".formatted(savedFriendship.getId()),
                HttpStatus.CREATED);
    }

    @PostMapping("/api/friends/response")
    public ResponseEntity<?> respondFriendRequest(@RequestBody FriendResponseDto friendResponseDto,
            Principal principal) {
        Optional<Friendship> friendship = friendshipRepository.findById(friendResponseDto.getFriendshipId());
        // check if friendship record exists
        if (!friendship.isPresent()) {
            return new ResponseEntity<String>(
                    "Friendship record with id %d does not exist.".formatted(friendResponseDto.getFriendshipId()),
                    HttpStatus.BAD_REQUEST);
        }

        Friendship f = friendship.get();

        // check if friendship request has been processed
        if (f.getStatus() != FriendshipStatus.PENDING) {
            return new ResponseEntity<String>("Friendship request already responded.", HttpStatus.BAD_REQUEST);
        }

        // check if the current user is eligible for responding (user2 is the recipient
        // of the friend request)
        if (!f.getUser2().getUsername().equals(principal.getName())) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }

        String response = friendResponseDto.getResponse();
        if (response.equalsIgnoreCase(FriendshipStatus.ACCEPTED.name())) {
            f.setStatus(FriendshipStatus.ACCEPTED);
        } else if (response.equalsIgnoreCase(FriendshipStatus.REJECTED.name())) {
            f.setStatus(FriendshipStatus.REJECTED);
        } else {
            return new ResponseEntity<String>("Invalid response.", HttpStatus.BAD_REQUEST);
        }

        friendshipRepository.save(f);

        // save new friend document to index
        if (f.getStatus() == FriendshipStatus.ACCEPTED) {
            FriendDoc friendDoc = new FriendDoc();
            friendDoc.setUser1Id(f.getUser1().getId());
            friendDoc.setUser2Id(f.getUser2().getId());
            friendDoc.setUser1Fullname(f.getUser1().getFullname());
            friendDoc.setUser2Fullname(f.getUser2().getFullname());
            try {
                friendSearchService.createFriendIndex(friendDoc);
            } catch (IOException e) {
                return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
            }
        }

        // make response notification here

        return new ResponseEntity<>(HttpStatus.OK);
    }

    @GetMapping("/api/friends/list")
    public ResponseEntity<List<UserDto>> listFriends(Principal principal) {
        User user = userRepostitory.findByUsername(principal.getName()).get();

        List<Friendship> outgoingFriendships = friendshipRepository.findByUser1(user);
        List<Friendship> incomingFriendships = friendshipRepository.findByUser2(user);

        List<UserDto> friends = new ArrayList<>();

        for (Friendship f : outgoingFriendships) {
            if (f.getStatus() == FriendshipStatus.ACCEPTED) {
                UserDto friend = new UserDto();
                friend.setId(f.getUser2().getId());
                friend.setFullname(f.getUser2().getFullname());
                friend.setEmail(f.getUser2().getEmail());
                friends.add(friend);
            }
        }

        for (Friendship f : incomingFriendships) {
            if (f.getStatus() == FriendshipStatus.ACCEPTED) {
                UserDto friend = new UserDto();
                friend.setId(f.getUser1().getId());
                friend.setFullname(f.getUser1().getFullname());
                friend.setEmail(f.getUser1().getEmail());
                friends.add(friend);
            }
        }

        return new ResponseEntity<>(friends, HttpStatus.OK);
    }

    @GetMapping("/api/friends/search")
    public ResponseEntity<List<UserDto>> searchForFriends(@RequestBody FriendSearchDto friendSearchDto,
            Principal principal) {
        User currentUser = userRepostitory.findByUsername(principal.getName()).get();
        List<User> friends = null;

        try {
            friends = friendSearchService.searchForFriends(currentUser.getId(), friendSearchDto.getQuery());
        } catch (IOException e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
        List<UserDto> friendsReponse = new ArrayList<>();
        for (User friend : friends) {
            UserDto user = new UserDto();
            user.setId(friend.getId());
            user.setFullname(friend.getFullname());
            user.setEmail(friend.getEmail());
            friendsReponse.add(user);
        }

        return new ResponseEntity<>(friendsReponse, HttpStatus.OK);
    }
}
