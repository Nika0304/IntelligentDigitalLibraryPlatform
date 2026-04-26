package com.library.repository;

import com.library.model.Notification;
import com.library.model.NotificationType;
import com.library.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long>{
    List<Notification> findByUser(User user);
    List<Notification> findByUserAndIsReadFalse(User user);
    List<Notification> findByUserAndType(User user, NotificationType type);
    List<Notification> findByUserAndTypeAndIsReadFalse(User user, NotificationType type);
}
