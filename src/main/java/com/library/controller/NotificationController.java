package com.library.controller;

import com.library.dto.NotificationRequest;
import com.library.model.Notification;
import com.library.model.NotificationType;
import com.library.service.NotificationService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController
{

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService)
    {
        this.notificationService = notificationService;
    }

    @GetMapping
    public ResponseEntity<?> getAllNotifications()
    {
        try
        {
            List<Notification> notifications = notificationService.getAllNotifications();
            return ResponseEntity.ok(notifications);
        }
        catch (Exception e)
        {
            return handleGenericException(e);
        }
    }

    @GetMapping("/{notificationId}")
    public ResponseEntity<?> getNotificationById(@PathVariable Long notificationId)
    {
        try
        {
            Notification notification = notificationService.getNotificationById(notificationId);
            return ResponseEntity.ok(notification);
        }
        catch (IllegalArgumentException e)
        {
            return handleBadRequestException(e);
        }
        catch (RuntimeException e)
        {
            return handleNotFoundException(e);
        }
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<?> getNotificationsByUserId(@PathVariable Long userId)
    {
        try
        {
            List<Notification> notifications = notificationService.getNotificationsByUserId(userId);
            return ResponseEntity.ok(notifications);
        }
        catch (IllegalArgumentException e)
        {
            return handleBadRequestException(e);
        }
        catch (RuntimeException e)
        {
            return handleNotFoundException(e);
        }
    }

    @GetMapping("/user/{userId}/unread")
    public ResponseEntity<?> getUnreadNotificationsByUserId(@PathVariable Long userId)
    {
        try
        {
            List<Notification> notifications = notificationService.getUnreadNotificationsByUserId(userId);
            return ResponseEntity.ok(notifications);
        }
        catch (IllegalArgumentException e)
        {
            return handleBadRequestException(e);
        }
        catch (RuntimeException e)
        {
            return handleNotFoundException(e);
        }
    }

    @GetMapping("/user/{userId}/type/{type}")
    public ResponseEntity<?> getNotificationsByUserIdAndType(@PathVariable Long userId,
                                                             @PathVariable NotificationType type)
    {
        try
        {
            List<Notification> notifications = notificationService.getNotificationsByUserIdAndType(userId, type);
            return ResponseEntity.ok(notifications);
        }
        catch (IllegalArgumentException e)
        {
            return handleBadRequestException(e);
        }
        catch (RuntimeException e)
        {
            return handleNotFoundException(e);
        }
    }

    @GetMapping("/user/{userId}/type/{type}/unread")
    public ResponseEntity<?> getUnreadNotificationsByUserIdAndType(@PathVariable Long userId,
                                                                   @PathVariable NotificationType type)
    {
        try
        {
            List<Notification> notifications = notificationService.getUnreadNotificationsByUserIdAndType(userId, type);
            return ResponseEntity.ok(notifications);
        }
        catch (IllegalArgumentException e)
        {
            return handleBadRequestException(e);
        }
        catch (RuntimeException e)
        {
            return handleNotFoundException(e);
        }
    }

    @PostMapping
    public ResponseEntity<?> createNotification(@RequestBody NotificationRequest request)
    {
        try
        {
            Notification createdNotification = notificationService.createNotification(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdNotification);
        }
        catch (IllegalArgumentException e)
        {
            return handleBadRequestException(e);
        }
        catch (RuntimeException e)
        {
            return handleNotFoundException(e);
        }
    }

    @PutMapping("/{notificationId}/read")
    public ResponseEntity<?> markAsRead(@PathVariable Long notificationId)
    {
        try
        {
            Notification notification = notificationService.markAsRead(notificationId);
            return ResponseEntity.ok(notification);
        }
        catch (IllegalArgumentException e)
        {
            return handleBadRequestException(e);
        }
        catch (RuntimeException e)
        {
            return handleNotFoundException(e);
        }
    }

    @PutMapping("/user/{userId}/read-all")
    public ResponseEntity<?> markAllAsReadForUser(@PathVariable Long userId)
    {
        try
        {
            notificationService.markAllAsReadForUser(userId);
            return ResponseEntity.noContent().build();
        }
        catch (IllegalArgumentException e)
        {
            return handleBadRequestException(e);
        }
        catch (RuntimeException e)
        {
            return handleNotFoundException(e);
        }
    }

    @DeleteMapping("/{notificationId}")
    public ResponseEntity<?> deleteNotification(@PathVariable Long notificationId)
    {
        try
        {
            notificationService.deleteNotification(notificationId);
            return ResponseEntity.noContent().build();
        }
        catch (IllegalArgumentException e)
        {
            return handleBadRequestException(e);
        }
        catch (RuntimeException e)
        {
            return handleNotFoundException(e);
        }
    }

    private ResponseEntity<String> handleBadRequestException(Exception e)
    {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
    }

    private ResponseEntity<String> handleNotFoundException(Exception e)
    {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
    }

    private ResponseEntity<String> handleConflictException(Exception e)
    {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
    }

    private ResponseEntity<String> handleGenericException(Exception e)
    {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("An unexpected error occurred");
    }
}