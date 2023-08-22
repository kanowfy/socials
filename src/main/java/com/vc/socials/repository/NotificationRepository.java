package com.vc.socials.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.vc.socials.model.Notification;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

}
