package com.library.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "challenges")
public class Challenge {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long challengeId;

    @Column(nullable = false)
    private String title;

    @Column(length = 1000)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ChallengeType type;

    @Column(nullable = false)
    private int targetCount;

    private Long categoryId;   // doar pentru READ_FROM_CATEGORY
    private Long authorId;     // doar pentru READ_FROM_AUTHOR

    @Column(nullable = false)
    private LocalDateTime startDate;
    @Column(nullable = false)
    private LocalDateTime endDate;

    private String iconEmoji;
    private boolean active = true;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public Challenge() {}

    @PrePersist
    protected void onCreate() { if (createdAt == null) createdAt = LocalDateTime.now(); }

    public Long getChallengeId() { return challengeId; }
    public void setChallengeId(Long id) { this.challengeId = id; }
    public String getTitle() { return title; }
    public void setTitle(String t) { this.title = t; }
    public String getDescription() { return description; }
    public void setDescription(String d) { this.description = d; }
    public ChallengeType getType() { return type; }
    public void setType(ChallengeType t) { this.type = t; }
    public int getTargetCount() { return targetCount; }
    public void setTargetCount(int c) { this.targetCount = c; }
    public Long getCategoryId() { return categoryId; }
    public void setCategoryId(Long id) { this.categoryId = id; }
    public Long getAuthorId() { return authorId; }
    public void setAuthorId(Long id) { this.authorId = id; }
    public LocalDateTime getStartDate() { return startDate; }
    public void setStartDate(LocalDateTime d) { this.startDate = d; }
    public LocalDateTime getEndDate() { return endDate; }
    public void setEndDate(LocalDateTime d) { this.endDate = d; }
    public String getIconEmoji() { return iconEmoji; }
    public void setIconEmoji(String e) { this.iconEmoji = e; }
    public boolean isActive() { return active; }
    public void setActive(boolean a) { this.active = a; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime d) { this.createdAt = d; }
}