package com.library.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "faq_entries")
public class FaqEntry
{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long faqId;

    @Column(nullable = false, length = 500)
    private String question;

    @Column(nullable = false, length = 2000)
    private String answer;

    @Column(nullable = false, length = 500)
    private String keywords; // comma-separated, lowercased

    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    public FaqEntry() {}
    public FaqEntry(String question, String answer, String keywords)
    {
        this.question = question;
        this.answer = answer;
        this.keywords = keywords;
        this.createdAt = LocalDateTime.now();
    }

    public Long getFaqId() { return faqId; }
    public String getQuestion() { return question; }
    public void setQuestion(String q) { this.question = q; }
    public String getAnswer() { return answer; }
    public void setAnswer(String a) { this.answer = a; }
    public String getKeywords() { return keywords; }
    public void setKeywords(String k) { this.keywords = k; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}