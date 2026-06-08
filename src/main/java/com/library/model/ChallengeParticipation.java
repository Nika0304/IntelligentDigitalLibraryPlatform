package com.library.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "challenge_participations",
        uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "challenge_id"}))
public class ChallengeParticipation {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long participationId;

    @ManyToOne(optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(optional = false)
    @JoinColumn(name = "challenge_id", nullable = false)
    private Challenge challenge;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ParticipationStatus status = ParticipationStatus.IN_PROGRESS;

    @Column(nullable = false, updatable = false)
    private LocalDateTime joinedAt;

    private LocalDateTime completedAt;

    public ChallengeParticipation() {}

    @PrePersist
    protected void onCreate() { if (joinedAt == null) joinedAt = LocalDateTime.now(); }

    public Long getParticipationId() { return participationId; }
    public void setParticipationId(Long id) { this.participationId = id; }
    public User getUser() { return user; }
    public void setUser(User u) { this.user = u; }
    public Challenge getChallenge() { return challenge; }
    public void setChallenge(Challenge c) { this.challenge = c; }
    public ParticipationStatus getStatus() { return status; }
    public void setStatus(ParticipationStatus s) { this.status = s; }
    public LocalDateTime getJoinedAt() { return joinedAt; }
    public void setJoinedAt(LocalDateTime d) { this.joinedAt = d; }
    public LocalDateTime getCompletedAt() { return completedAt; }
    public void setCompletedAt(LocalDateTime d) { this.completedAt = d; }
}