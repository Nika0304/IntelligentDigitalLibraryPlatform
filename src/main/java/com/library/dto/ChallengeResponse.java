package com.library.dto;

import com.library.model.ChallengeType;
import java.time.LocalDateTime;

public class ChallengeResponse {
    private Long challengeId;
    private String title;
    private String description;
    private ChallengeType type;
    private int targetCount;
    private Long categoryId;
    private String categoryName;
    private Long authorId;
    private String authorName;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private String iconEmoji;
    private boolean active;
    private long participantsCount;

    // user-specific (poate fi null pentru utilizatorii anonimi)
    private Boolean joined;
    private Integer userProgress;
    private String userStatus;          // IN_PROGRESS, COMPLETED, ABANDONED
    private LocalDateTime userJoinedAt;
    private LocalDateTime userCompletedAt;

    public ChallengeResponse() {}

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
    public String getCategoryName() { return categoryName; }
    public void setCategoryName(String n) { this.categoryName = n; }
    public Long getAuthorId() { return authorId; }
    public void setAuthorId(Long id) { this.authorId = id; }
    public String getAuthorName() { return authorName; }
    public void setAuthorName(String n) { this.authorName = n; }
    public LocalDateTime getStartDate() { return startDate; }
    public void setStartDate(LocalDateTime d) { this.startDate = d; }
    public LocalDateTime getEndDate() { return endDate; }
    public void setEndDate(LocalDateTime d) { this.endDate = d; }
    public String getIconEmoji() { return iconEmoji; }
    public void setIconEmoji(String e) { this.iconEmoji = e; }
    public boolean isActive() { return active; }
    public void setActive(boolean a) { this.active = a; }
    public long getParticipantsCount() { return participantsCount; }
    public void setParticipantsCount(long c) { this.participantsCount = c; }
    public Boolean getJoined() { return joined; }
    public void setJoined(Boolean j) { this.joined = j; }
    public Integer getUserProgress() { return userProgress; }
    public void setUserProgress(Integer p) { this.userProgress = p; }
    public String getUserStatus() { return userStatus; }
    public void setUserStatus(String s) { this.userStatus = s; }
    public LocalDateTime getUserJoinedAt() { return userJoinedAt; }
    public void setUserJoinedAt(LocalDateTime d) { this.userJoinedAt = d; }
    public LocalDateTime getUserCompletedAt() { return userCompletedAt; }
    public void setUserCompletedAt(LocalDateTime d) { this.userCompletedAt = d; }
}