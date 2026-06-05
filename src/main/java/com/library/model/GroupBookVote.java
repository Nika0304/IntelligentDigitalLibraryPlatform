package com.library.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.time.YearMonth;

@Entity
@Table(name = "group_book_votes",
        uniqueConstraints = @UniqueConstraint(columnNames = {"group_id", "user_id", "period"}))
public class GroupBookVote
{
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long voteId;

    @ManyToOne(optional = false) @JoinColumn(name = "group_id", nullable = false)
    private BookGroup group;

    @ManyToOne(optional = false) @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(optional = false) @JoinColumn(name = "book_id", nullable = false)
    private Book book;

    @Column(nullable = false, length = 7)
    private String period; // YYYY-MM

    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    public GroupBookVote() {}
    public GroupBookVote(BookGroup g, User u, Book b)
    {
        this.group = g; this.user = u; this.book = b;
        this.period = YearMonth.now().toString();
        this.createdAt = LocalDateTime.now();
    }

    public Long getVoteId() { return voteId; }
    public BookGroup getGroup() { return group; }
    public User getUser() { return user; }
    public Book getBook() { return book; }
    public void setBook(Book b) { this.book = b; }
    public String getPeriod() { return period; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}