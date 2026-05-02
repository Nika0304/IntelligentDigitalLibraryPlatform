package com.library.service;

import com.library.dto.NotificationRequest;
import com.library.model.Notification;
import com.library.model.NotificationType;
import com.library.model.User;
import com.library.repository.NotificationRepository;
import com.library.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class NotificationService
{

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    public NotificationService(NotificationRepository notificationRepository, UserRepository userRepository)
    {
        this.notificationRepository = notificationRepository;
        this.userRepository = userRepository;
    }

    public List<Notification> getAllNotifications()
    {
        return notificationRepository.findAll();
    }

    public Notification getNotificationById(Long notificationId)
    {
        validateId(notificationId, "Notification id");

        return notificationRepository.findById(notificationId)
                .orElseThrow(() -> new RuntimeException("Notification not found with id: " + notificationId));
    }

    public List<Notification> getNotificationsByUserId(Long userId)
    {
        User user = getUserOrThrow(userId);

        return notificationRepository.findByUserOrderByCreatedAtDesc(user);
    }

    public List<Notification> getUnreadNotificationsByUserId(Long userId)
    {
        User user = getUserOrThrow(userId);

        return notificationRepository.findByUserAndIsReadFalseOrderByCreatedAtDesc(user);
    }

    public List<Notification> getNotificationsByUserIdAndType(Long userId, NotificationType type)
    {
        validateNotificationType(type);

        User user = getUserOrThrow(userId);

        return notificationRepository.findByUserAndTypeOrderByCreatedAtDesc(user, type);
    }

    public List<Notification> getUnreadNotificationsByUserIdAndType(Long userId, NotificationType type)
    {
        validateNotificationType(type);

        User user = getUserOrThrow(userId);

        return notificationRepository.findByUserAndTypeAndIsReadFalseOrderByCreatedAtDesc(user, type);
    }

    public Notification createNotification(NotificationRequest request)
    {
        validateNotificationRequest(request);

        User user = getUserOrThrow(request.getUserId());

        Notification notification = new Notification();
        notification.setUser(user);
        notification.setMessage(normalizeMessage(request.getMessage()));
        notification.setType(request.getType());
        notification.setRead(false);

        return notificationRepository.save(notification);
    }

    public Notification markAsRead(Long notificationId)
    {
        validateId(notificationId, "Notification id");

        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new RuntimeException("Notification not found with id: " + notificationId));

        notification.setRead(true);

        return notificationRepository.save(notification);
    }

    public void markAllAsReadForUser(Long userId)
    {
        User user = getUserOrThrow(userId);

        List<Notification> unreadNotifications =
                notificationRepository.findByUserAndIsReadFalseOrderByCreatedAtDesc(user);

        for (Notification notification : unreadNotifications)
        {
            notification.setRead(true);
        }

        notificationRepository.saveAll(unreadNotifications);
    }

    public void deleteNotification(Long notificationId)
    {
        validateId(notificationId, "Notification id");

        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new RuntimeException("Notification not found with id: " + notificationId));

        notificationRepository.delete(notification);
    }

    public Notification createSystemNotification(Long userId, String message, NotificationType type)
    {
        validateId(userId, "User id");
        validateMessage(message);
        validateNotificationType(type);

        User user = getUserOrThrow(userId);

        Notification notification = new Notification();
        notification.setUser(user);
        notification.setMessage(normalizeMessage(message));
        notification.setType(type);
        notification.setRead(false);

        return notificationRepository.save(notification);
    }

    private User getUserOrThrow(Long userId)
    {
        validateId(userId, "User id");

        return userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));
    }

    private void validateNotificationRequest(NotificationRequest request)
    {
        if (request == null)
        {
            throw new IllegalArgumentException("Notification request is required");
        }

        validateId(request.getUserId(), "User id");
        validateMessage(request.getMessage());
        validateNotificationType(request.getType());
    }

    private void validateId(Long id, String fieldName)
    {
        if (id == null)
        {
            throw new IllegalArgumentException(fieldName + " is required");
        }

        if (id <= 0)
        {
            throw new IllegalArgumentException(fieldName + " must be greater than 0");
        }
    }

    private void validateMessage(String message)
    {
        if (message == null || message.trim().isEmpty())
        {
            throw new IllegalArgumentException("Notification message is required");
        }

        if (message.length() > 1000)
        {
            throw new IllegalArgumentException("Notification message cannot exceed 1000 characters");
        }
    }

    private void validateNotificationType(NotificationType type)
    {
        if (type == null)
        {
            throw new IllegalArgumentException("Notification type is required");
        }
    }

    private String normalizeMessage(String message)
    {
        return message.trim();
    }
}