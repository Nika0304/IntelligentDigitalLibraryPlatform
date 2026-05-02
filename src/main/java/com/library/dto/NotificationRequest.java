package com.library.dto;

import com.library.model.NotificationType;

public class NotificationRequest
{
    private Long userId;
    private String message;
    private NotificationType type;

    public NotificationRequest() {}

    public Long getUserId()
    {
        return userId;
    }

    public void setUserId(Long userId)
    {
        this.userId = userId;
    }

    public String getMessage()
    {
        return message;
    }

    public void setMessage(String message)
    {
        this.message = message;
    }

    public NotificationType getType()
    {
        return type;
    }

    public void setType(NotificationType type)
    {
        this.type = type;
    }
}