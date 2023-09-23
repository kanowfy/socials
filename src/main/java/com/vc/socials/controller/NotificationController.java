package com.vc.socials.controller;

import com.vc.socials.dto.NotificationDto;
import com.vc.socials.model.*;
import com.vc.socials.service.NotificationService;
import com.vc.socials.service.UserService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Tag(name = "notification", description = "notification API")
@RestController
public class NotificationController {
    private NotificationService notificationService;

    private UserService userService;

    public NotificationController(NotificationService notificationService,
            UserService userService) {
        this.notificationService = notificationService;
        this.userService = userService;
    }

    @Operation(summary = "Get all notifications of the current user")
    @ApiResponses(value = { @ApiResponse(responseCode = "400", description = "Invalid user"),
            @ApiResponse(responseCode = "200", description = "Operation successful") })
    @GetMapping("/api/notifications")
    public ResponseEntity<?> getNotificationsOfUser(Principal principal) {
        Optional<User> user = userService.getUserByUserName(principal.getName());
        if (user.isEmpty())
            return new ResponseEntity<>("Invalid User", HttpStatus.BAD_REQUEST);
        List<Notification> notificationList = notificationService.getNotificationsByUserID(user.get());
        List<NotificationDto> notificationDtoList = notification2Dto(notificationList);
        return new ResponseEntity<>(notificationDtoList, HttpStatus.OK);
    }

    @Operation(summary = "Get notification by ID")
    @ApiResponses(value = { @ApiResponse(responseCode = "400", description = "Invalid notification ID"),
            @ApiResponse(responseCode = "200", description = "Operation successful") })
    @GetMapping("/api/notifications/{id}")
    public ResponseEntity<?> getNotificationByID(@PathVariable("id") Long id) {
        List<Notification> notificationList = new ArrayList<>();
        Optional<Notification> notification = notificationService.getNotificationByID(id);
        if (notification.isEmpty())
            return new ResponseEntity<>("Invalid notification_id", HttpStatus.BAD_REQUEST);
        notificationList.add(notification.get());
        List<NotificationDto> notificationDtoList = notification2Dto(notificationList);
        return new ResponseEntity<>(notificationDtoList, HttpStatus.OK);
    }

    private List<NotificationDto> notification2Dto(List<Notification> notificationList) {
        List<NotificationDto> notificationDtoList = new ArrayList<>();
        for (Notification notification : notificationList) {
            NotificationDto notificationDto = new NotificationDto();
            notificationDto.setNotificationId(notification.getId());
            notificationDto.setUserId(notification.getUser().getId());
            notificationDto.setRead(notification.getIsRead());
            notificationDto.setNotificationType(notification.getType());
            notificationDto.setCreatedAt(notification.getCreatedAt());
            if (notification.getType() == NotificationType.COMMENT) {
                notificationDto.setCommentId(notification.getComment().getId());
            } else {
                notificationDto.setSenderId(notification.getSender().getId());
            }
            notificationDtoList.add(notificationDto);
        }
        return notificationDtoList;
    }
}
