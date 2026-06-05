package com.library.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "chat_questions")
public class ChatQuestion
{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long questionId;

    @ManyToOne(optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    @JsonIgnoreProperties({"password", "role"})
    private User user;

    @Column(nullable = false, length = 1000)
    private String message;

    @Column(length = 2000)
    private String answer;

    @Column(nullable = false, length = 20)
    private String status = "PENDING"; // PENDING / ANSWERED

    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    private LocalDateTime answeredAt;

    public ChatQuestion() {}
    public ChatQuestion(User u, String msg)
    {
        this.user = u;
        this.message = msg;
        this.status = "PENDING";
        this.createdAt = LocalDateTime.now();
    }

    public Long getQuestionId() { return questionId; }
    public User getUser() { return user; }
    public String getMessage() { return message; }
    public String getAnswer() { return answer; }
    public void setAnswer(String a) { this.answer = a; }
    public String getStatus() { return status; }
    public void setStatus(String s) { this.status = s; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getAnsweredAt() { return answeredAt; }
    public void setAnsweredAt(LocalDateTime t) { this.answeredAt = t; }
}