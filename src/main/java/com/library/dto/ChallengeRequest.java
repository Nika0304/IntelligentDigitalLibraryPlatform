package com.library.dto;

import com.library.model.ChallengeType;
import java.time.LocalDateTime;

public class ChallengeRequest {
    private String title;
    private String description;
    private ChallengeType type;
    private int targetCount;
    private Long categoryId;
    private Long authorId;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private String iconEmoji;

    public ChallengeRequest() {}

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
}