package com.vc.socials.controller;

import java.io.IOException;
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

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

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
import com.vc.socials.service.FriendSearchService;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@Tag(name = "friend", description = "friend API")
@RestController
public class FriendController {
    private UserService userService;
    private FriendshipService friendshipService;
    private FriendSearchService friendSearchService;

    private KafkaProducerService producerService;

    public FriendController(UserService userService, FriendshipService friendshipService,
            FriendSearchService friendSearchService, KafkaProducerService producerService) {
        this.userService = userService;
        this.friendshipService = friendshipService;
        this.friendSearchService = friendSearchService;
        this.producerService = producerService;
    }

    @Operation(summary = "Send friend request to another user using ID")
    @ApiResponses(value = { @ApiResponse(responseCode = "404", description = "Invalid ID supplied"),
            @ApiResponse(responseCode = "400", description = "Friend record exists"),
            @ApiResponse(responseCode = "201", description = "Friend request created") })
    @PostMapping("/api/friends/request")
    public ResponseEntity<?> makeFriendRequest(@RequestBody FriendRequestDto friendRequestDto, Principal principal) {
        Optional<User> user1 = userService.getUserByUserName(principal.getName());
        Optional<User> user2 = userService.getUserById(friendRequestDto.getToUserId());

        if (!user2.isPresent()) {
            return new ResponseEntity<String>(
                    "User with id %d does not exist.".formatted(friendRequestDto.getToUserId()),
                    HttpStatus.NOT_FOUND);
        }

        // check if friendship record exists between the 2 users
        if (friendshipService.getFriendshipByUsersID(user1.get().getId(), user2.get().getId())
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
        notification.setUserId(user2.get().getId());
        notification.setSenderId(user1.get().getId());
        notification.setCreatedAt(Timestamp.from(Instant.now()));
        notification.setRead(false);
        // notificationService.saveNotification(notification);
        // send to kafka
        producerService.sendNotification(notification);

        return new ResponseEntity<String>("Friend request created with id %d.".formatted(savedFriendship.getId()),
                HttpStatus.CREATED);
    }

    @Operation(summary = "Respond to a friend request")
    @ApiResponses(value = { @ApiResponse(responseCode = "404", description = "Invalid friendship ID"),
            @ApiResponse(responseCode = "400", description = "Friend request already responded"),
            @ApiResponse(responseCode = "403", description = "Can not respond to friend request of others"),
            @ApiResponse(responseCode = "200", description = "Operation successful") })
    @PostMapping("/api/friends/response")
    public ResponseEntity<?> respondFriendRequest(@RequestBody FriendResponseDto friendResponseDto,
            Principal principal) {
        Optional<Friendship> friendship = friendshipService.getFriendshipByID(friendResponseDto.getFriendshipId());
        // check if friendship record exists
        if (!friendship.isPresent()) {
            return new ResponseEntity<String>(
                    "Friendship record with id %d does not exist.".formatted(friendResponseDto.getFriendshipId()),
                    HttpStatus.NOT_FOUND);
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
        NotificationDto notification = new NotificationDto();
        if (response.equalsIgnoreCase(FriendshipStatus.ACCEPTED.name())) {
            notification.setNotificationType(NotificationType.FRIEND_ACCEPTED);
        } else {
            notification.setNotificationType(NotificationType.FRIEND_REJECTED);
        }
        notification.setUserId(f.getUser1().getId());
        notification.setSenderId(f.getUser2().getId());
        notification.setCreatedAt(Timestamp.from(Instant.now()));
        notification.setRead(false);
        // send to kafka
        producerService.sendNotification(notification);

        return new ResponseEntity<>(HttpStatus.OK);
    }

    @Operation(summary = "List all friends")
    @ApiResponse(responseCode = "200", description = "Operation successful")
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

    @Operation(summary = "Search for friends")
    @ApiResponses(value = { @ApiResponse(responseCode = "500", description = "Server error"),
            @ApiResponse(responseCode = "200", description = "Operation successful") })
    @GetMapping("/api/friends/search")
    public ResponseEntity<List<UserDto>> searchForFriends(@RequestBody FriendSearchDto friendSearchDto,
            Principal principal) {
        User currentUser = userService.getUserByUserName(principal.getName()).get();
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
