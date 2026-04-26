package com.library.model;
import jakarta.persistence.*;

@Entity
@Table(name  = "roles")

public class Role {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long roleId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, unique = true)
    private RoleType roleName;

    public Role(){}

    public Role(RoleType roleName)
    {
        this.roleName = roleName;
    }

    public Long getRoleId()
    {
        return roleId;
    }

    public void setRoleId(Long roleId)
    {
        this.roleId = roleId;
    }

    public RoleType getRoleName()
    {
        return roleName;
    }

    public void setRoleName(RoleType roleName)
    {
        this.roleName = roleName;
    }
}
