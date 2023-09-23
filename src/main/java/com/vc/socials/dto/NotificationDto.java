package com.vc.socials.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.vc.socials.model.NotificationType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.sql.Timestamp;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class NotificationDto {
    @JsonProperty("notification_id")
    private Long notificationId;
    @JsonProperty("user_id")
    private Long userId;
    private NotificationType notificationType;
    @JsonProperty("sender_id")
    private Long senderId;
    @JsonProperty("comment_id")
    private Long commentId;
    @JsonProperty("is_read")
    private boolean isRead;
    @JsonProperty("created_at")
    private Timestamp createdAt;
}
