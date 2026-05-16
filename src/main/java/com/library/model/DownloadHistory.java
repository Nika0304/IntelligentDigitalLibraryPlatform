package com.library.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "download_history")
public class DownloadHistory
{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long downloadId;

    @ManyToOne(optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    @JsonIgnore
    private User user;

    @ManyToOne(optional = false)
    @JoinColumn(name = "book_id", nullable = false)
    private Book book;

    @Column(nullable = false, updatable = false)
    private LocalDateTime downloadDate;

    public DownloadHistory() {}

    public DownloadHistory(User user, Book book)
    {
        this.user = user;
        this.book = book;
        this.downloadDate = LocalDateTime.now();
    }

    @PrePersist
    protected void onCreate()
    {
        if (downloadDate == null)
        {
            downloadDate = LocalDateTime.now();
        }
    }

    public Long getDownloadId() { return downloadId; }
    public void setDownloadId(Long downloadId) { this.downloadId = downloadId; }
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
    public Book getBook() { return book; }
    public void setBook(Book book) { this.book = book; }
    public LocalDateTime getDownloadDate() { return downloadDate; }
    public void setDownloadDate(LocalDateTime downloadDate) { this.downloadDate = downloadDate; }
}
