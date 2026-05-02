package com.library.dto;

import com.library.model.UserStatus;

public class UserStatusUpdateRequest
{
    private UserStatus status;

    public UserStatusUpdateRequest() {}

    public UserStatus getStatus()
    {
        return status;
    }

    public void setStatus(UserStatus status)
    {
        this.status = status;
    }
}