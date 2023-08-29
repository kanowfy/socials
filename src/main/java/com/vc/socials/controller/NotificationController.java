package com.vc.socials.controller;

import com.vc.socials.dto.CommentDto;
import com.vc.socials.dto.NotificationDto;
import com.vc.socials.dto.PostDto;
import com.vc.socials.model.*;
import com.vc.socials.service.NotificationService;
import com.vc.socials.service.PostService;
import com.vc.socials.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import javax.swing.text.html.Option;
import java.security.Principal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@RestController
public class NotificationController {
    private NotificationService notificationService;

    private UserService userService;

    private PostService postService;

    @Autowired
    public NotificationController(NotificationService notificationService,
                                  UserService userService, PostService postService){
        this.notificationService = notificationService;
        this.userService = userService;
        this.postService = postService;
    }

    @GetMapping("/api/notifications")
    public ResponseEntity<?> getNotificationsOfUser(Principal principal){
        Optional<User> user = userService.getUserByUserName(principal.getName());
        if (user.isEmpty()) return new ResponseEntity<>("Invalid User", HttpStatus.BAD_REQUEST);
        List<Notification> notificationList = notificationService.getNotificationsByUserID(user.get());
        List<NotificationDto> notificationDtoList = notification2Dto(notificationList);
        return new ResponseEntity<>(notificationDtoList, HttpStatus.OK);
    }

    @GetMapping("/api/notifications/{id}")
    public ResponseEntity<?> getNotificationByID(@PathVariable("id") Long id){
        List<Notification> notificationList = new ArrayList<>();
        Optional<Notification> notification = notificationService.getNotificationByID(id);
        if (notification.isEmpty()) return new ResponseEntity<>("Invalid notification_id", HttpStatus
                .BAD_REQUEST);
        notificationList.add(notification.get());
        List<NotificationDto> notificationDtoList = notification2Dto(notificationList);
        return new ResponseEntity<>(notificationDtoList, HttpStatus.OK);
    }

    private List<NotificationDto> notification2Dto(List<Notification> notificationList){
        List<NotificationDto> notificationDtoList = new ArrayList<>();
        for (Notification notification : notificationList){
            NotificationDto notificationDto = new NotificationDto();
            notificationDto.setNotification_id(notification.getId());
            notificationDto.setUser_id(notification.getUser().getId());
            notificationDto.set_read(notification.getIsRead());
            notificationDto.setNotificationType(notification.getType());
            notificationDto.setCreated_at(notification.getCreatedAt());
            if (notification.getType() == NotificationType.COMMENT){
                notificationDto.setComment_id(notification.getComment().getId());
            } else{
                notificationDto.setSender_id(notification.getSender().getId());
            }
            notificationDtoList.add(notificationDto);
        }
        return notificationDtoList;
    }
}
