package com.library.dto;

import com.library.model.RoleType;
import com.library.model.UserStatus;

public class UserResponse
{
    private Long userId;
    private String fullName;
    private String email;
    private UserStatus status;
    private Long roleId;
    private String roleName;

    public UserResponse() {}

    public UserResponse(Long userId, String fullName, String email, UserStatus status, Long roleId, String roleName)
    {
        this.userId = userId;
        this.fullName = fullName;
        this.email = email;
        this.status = status;
        this.roleId = roleId;
        this.roleName = roleName;
    }

    public Long getUserId()
    {
        return userId;
    }

    public void setUserId(Long userId)
    {
        this.userId = userId;
    }

    public String getFullName()
    {
        return fullName;
    }

    public void setFullName(String fullName)
    {
        this.fullName = fullName;
    }

    public String getEmail()
    {
        return email;
    }

    public void setEmail(String email)
    {
        this.email = email;
    }

    public UserStatus getStatus()
    {
        return status;
    }

    public void setStatus(UserStatus status)
    {
        this.status = status;
    }

    public Long getRoleId()
    {
        return roleId;
    }

    public void setRoleId(Long roleId)
    {
        this.roleId = roleId;
    }

    public String getRoleName()
    {
        return roleName;
    }

    public void setRoleName(String roleName)
    {
        this.roleName = roleName;
    }
}