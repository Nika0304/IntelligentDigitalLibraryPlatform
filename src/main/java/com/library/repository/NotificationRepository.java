package com.library.repository;

import com.library.model.Notification;
import com.library.model.NotificationType;
import com.library.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
    List<Notification> findByUserOrderByCreatedAtDesc(User user);
    List<Notification> findByUserAndIsReadFalseOrderByCreatedAtDesc(User user);
    List<Notification> findByUserAndTypeOrderByCreatedAtDesc(User user, NotificationType type);
    List<Notification> findByUserAndTypeAndIsReadFalseOrderByCreatedAtDesc(User user, NotificationType type);
}