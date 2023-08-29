package com.vc.socials.service;

import com.vc.socials.model.Notification;
import com.vc.socials.model.User;

import javax.swing.text.html.Option;
import java.util.List;
import java.util.Optional;

public interface NotificationService {
    public List<Notification> getNotificationsByUserID(User userID);

    public List<Notification> getNotificationsByUserID(User userID, int limit);

    public Optional<Notification> getNotificationByID(Long id);
    public void saveNotification(Notification notification);
}
