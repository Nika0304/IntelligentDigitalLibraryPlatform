package com.library.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "group_messages")
public class GroupMessage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long messageId;

    @ManyToOne(optional = false)
    @JoinColumn(name = "group_id", nullable = false)
    @JsonIgnoreProperties({"creator", "featuredBook"})
    private BookGroup group;

    @ManyToOne(optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    @JsonIgnoreProperties({"password", "role"})
    private User user;

    @Column(nullable = false, length = 1000)
    private String content;

    @Column(nullable = false)
    private boolean deleted = false;

    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    public GroupMessage() {
    }

    public GroupMessage(BookGroup g, User u, String content) {
        this.group = g;
        this.user = u;
        this.content = content;
        this.createdAt = LocalDateTime.now();
    }

    public Long getMessageId() {
        return messageId;
    }

    public BookGroup getGroup() {
        return group;
    }

    public User getUser() {
        return user;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String c) {
        this.content = c;
    }

    public boolean isDeleted() {
        return deleted;
    }

    public void setDeleted(boolean d) {
        this.deleted = d;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}