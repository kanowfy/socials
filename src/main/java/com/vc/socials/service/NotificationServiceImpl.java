package com.vc.socials.service;

import com.vc.socials.model.Notification;
import com.vc.socials.model.User;
import com.vc.socials.repository.NotificationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class NotificationServiceImpl implements NotificationService{
    private NotificationRepository notificationRepository;
    @Autowired
    public NotificationServiceImpl(NotificationRepository notificationRepository){
        this.notificationRepository = notificationRepository;
    }

    @Override
    @Transactional
    public List<Notification> getNotificationsByUserID(User userID) {
        return notificationRepository.getNotificationsByUserID(userID);
    }

    @Override
    @Transactional
    public List<Notification> getNotificationsByUserID(User userID, int limit) {
        Pageable pageable = PageRequest.of(0, limit);
        return notificationRepository.getNotificationsByUserIDWithPagination(userID, pageable);
    }

    @Override
    @Transactional
    public Optional<Notification> getNotificationByID(Long id) {
        return notificationRepository.findById(id);
    }

    @Override
    @Transactional
    public void saveNotification(Notification notification) {
        notificationRepository.save(notification);
    }
}
