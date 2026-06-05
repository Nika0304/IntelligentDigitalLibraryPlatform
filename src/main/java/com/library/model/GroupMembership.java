package com.library.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "group_memberships",
        uniqueConstraints = @UniqueConstraint(columnNames = {"group_id", "user_id"}))
public class GroupMembership
{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long membershipId;

    @ManyToOne(optional = false)
    @JoinColumn(name = "group_id", nullable = false)
    @JsonIgnoreProperties({"creator", "featuredBook"})
    private BookGroup group;

    @ManyToOne(optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    @JsonIgnoreProperties({"password", "role"})
    private User user;

    @Column(nullable = false, length = 20)
    private String role = "MEMBER"; // MEMBER / MODERATOR

    @Column(nullable = false)
    private LocalDateTime joinedAt = LocalDateTime.now();
    @Column(nullable = false)
    private boolean notificationsMuted = false;

    public boolean isNotificationsMuted() { return notificationsMuted; }
    public void setNotificationsMuted(boolean m) { this.notificationsMuted = m; }

    public GroupMembership() {}
    public GroupMembership(BookGroup g, User u, String role)
    {
        this.group = g;
        this.user = u;
        this.role = role;
        this.joinedAt = LocalDateTime.now();
    }

    public Long getMembershipId() { return membershipId; }
    public BookGroup getGroup() { return group; }
    public User getUser() { return user; }
    public String getRole() { return role; }
    public void setRole(String r) { this.role = r; }
    public LocalDateTime getJoinedAt() { return joinedAt; }
}