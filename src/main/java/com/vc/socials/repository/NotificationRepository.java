package com.vc.socials.repository;

import com.vc.socials.model.User;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.vc.socials.model.Notification;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
    @Query("from Notification as n where n.user = ?1  order by n.createdAt desc")
    public List<Notification> getNotificationsByUserID(User userID);
    @Query("from Notification as n where n.user = ?1  order by n.createdAt desc")
    public List<Notification> getNotificationsByUserIDWithPagination(User userID, Pageable pageable);

}
