package com.vc.socials.controller;

import java.security.Principal;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.vc.socials.dto.NotificationDto;
import com.vc.socials.model.NotificationType;
import com.vc.socials.service.FriendshipService;
import com.vc.socials.service.KafkaProducerService;
import com.vc.socials.service.UserService;
import org.aspectj.weaver.ast.Not;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.vc.socials.dto.FriendRequestDto;
import com.vc.socials.dto.FriendResponseDto;
import com.vc.socials.dto.UserDto;
import com.vc.socials.model.Friendship;
import com.vc.socials.model.FriendshipStatus;
import com.vc.socials.model.User;
import com.vc.socials.repository.FriendshipRepository;
import com.vc.socials.repository.UserRepostitory;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@RestController
public class FriendController {
    private UserService userService;
    private FriendshipService friendshipService;

    private KafkaProducerService producerService;

    public FriendController(UserService userService, FriendshipService friendshipService,
                            KafkaProducerService producerService) {
        this.userService = userService;
        this.friendshipService = friendshipService;
        this.producerService = producerService;
    }

    @GetMapping("/api/friends/hello")
    public ResponseEntity<String> sayHello(Principal principal) {
        return new ResponseEntity<String>(principal.getName(), HttpStatus.OK);
    }

    @PostMapping("/api/friends/request")
    public ResponseEntity<?> makeFriendRequest(@RequestBody FriendRequestDto friendRequestDto) {
        Optional<User> user1 = userService.getUserById(friendRequestDto.getFromUserId());
        Optional<User> user2 = userService.getUserById(friendRequestDto.getToUserId());

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
        if (friendshipService.getFriendshipByUsersID(friendRequestDto.getFromUserId(), friendRequestDto.getToUserId())
                .isPresent()) {
            return new ResponseEntity<String>("Friendship record already exists.", HttpStatus.BAD_REQUEST);
        }

        Friendship friendship = new Friendship();
        friendship.setUser1(user1.get());
        friendship.setUser2(user2.get());
        friendship.setStatus(FriendshipStatus.PENDING);
        friendship.setCreatedAt(Timestamp.from(Instant.now()));
        Friendship savedFriendship = friendshipService.saveFriendship(friendship);

        // make request notification here
        NotificationDto notification = new NotificationDto();
        notification.setNotificationType(NotificationType.FRIEND_REQUEST);
        notification.setUser_id(user2.get().getId());
        notification.setSender_id(user1.get().getId());
        notification.setCreated_at(Timestamp.from(Instant.now()));
        notification.set_read(false);
//        notificationService.saveNotification(notification);
        //send to kafka
        producerService.sendNotification(notification);

        return new ResponseEntity<String>("Friend request created with id %d.".formatted(savedFriendship.getId()),
                HttpStatus.CREATED);
    }

    @PostMapping("/api/friends/response")
    public ResponseEntity<?> respondFriendRequest(@RequestBody FriendResponseDto friendResponseDto,
            Principal principal) {
        Optional<Friendship> friendship = friendshipService.getFriendshipByID(friendResponseDto.getFriendshipId());
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

        friendshipService.saveFriendship(f);
        // TODO: save document to index here

        // make response notification here
        NotificationDto notification = new NotificationDto();
        if (response.equalsIgnoreCase(FriendshipStatus.ACCEPTED.name())) {
            notification.setNotificationType(NotificationType.FRIEND_ACCEPTED);
        } else {
            notification.setNotificationType(NotificationType.FRIEND_REJECTED);
        }
        notification.setUser_id(f.getUser1().getId());
        notification.setSender_id(f.getUser2().getId());
        notification.setCreated_at(Timestamp.from(Instant.now()));
        notification.set_read(false);
//        notificationService.saveNotification(notification);
        //send to kafka
        producerService.sendNotification(notification);

        return new ResponseEntity<>(HttpStatus.OK);
    }

    @GetMapping("/api/friends/list")
    public ResponseEntity<List<UserDto>> listFriends(Principal principal) {
        User user = userService.getUserByUserName(principal.getName()).get();

        List<Friendship> outgoingFriendships = friendshipService.getFriendshipsByUser1(user);
        List<Friendship> incomingFriendships = friendshipService.getFriendshipsByUser2(user);

        List<UserDto> friends = new ArrayList<>();

        for (Friendship f : outgoingFriendships) {
            if (f.getStatus() == FriendshipStatus.ACCEPTED) {
                UserDto friend = new UserDto();
                friend.setId(f.getUser2().getId());
                friend.setUsername(f.getUser2().getUsername());
                friend.setEmail(f.getUser2().getEmail());
                friends.add(friend);
            }
        }

        for (Friendship f : incomingFriendships) {
            if (f.getStatus() == FriendshipStatus.ACCEPTED) {
                UserDto friend = new UserDto();
                friend.setId(f.getUser1().getId());
                friend.setUsername(f.getUser1().getUsername());
                friend.setEmail(f.getUser1().getEmail());
                friends.add(friend);
            }
        }

        return new ResponseEntity<List<UserDto>>(friends, HttpStatus.OK);
    }
}
