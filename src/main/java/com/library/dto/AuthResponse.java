package com.library.dto;

import com.library.model.UserStatus;

public class AuthResponse
{
    private String token;
    private Long userId;
    private String fullName;
    private String email;
    private UserStatus status;
    private Long roleId;
    private String roleName;

    public AuthResponse() {}

    public AuthResponse(String token, Long userId, String fullName, String email,
                        UserStatus status, Long roleId, String roleName)
    {
        this.token = token;
        this.userId = userId;
        this.fullName = fullName;
        this.email = email;
        this.status = status;
        this.roleId = roleId;
        this.roleName = roleName;
    }

    public String getToken() { return token; }
    public Long getUserId() { return userId; }
    public String getFullName() { return fullName; }
    public String getEmail() { return email; }
    public UserStatus getStatus() { return status; }
    public Long getRoleId() { return roleId; }
    public String getRoleName() { return roleName; }

    public void setToken(String token) { this.token = token; }
    public void setUserId(Long userId) { this.userId = userId; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public void setEmail(String email) { this.email = email; }
    public void setStatus(UserStatus status) { this.status = status; }
    public void setRoleId(Long roleId) { this.roleId = roleId; }
    public void setRoleName(String roleName) { this.roleName = roleName; }
}