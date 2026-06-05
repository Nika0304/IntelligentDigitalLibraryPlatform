package com.library.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "book_groups")
public class BookGroup
{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long groupId;

    @Column(nullable = false, length = 120)
    private String name;

    @Column(nullable = false, length = 80)
    private String theme;

    @Column(nullable = false, length = 1000)
    private String description;

    @Column(nullable = false, length = 20)
    private String status = "PENDING"; // PENDING / APPROVED / REJECTED / ARCHIVED

    @ManyToOne(optional = false)
    @JoinColumn(name = "creator_id", nullable = false)
    @JsonIgnoreProperties({"password", "role"})
    private User creator;

    @ManyToOne
    @JoinColumn(name = "featured_book_id")
    @JsonIgnoreProperties({"authors", "reviews"})
    private Book featuredBook;

    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    private LocalDateTime decidedAt;

    public BookGroup() {}
    public BookGroup(String name, String theme, String description, User creator)
    {
        this.name = name;
        this.theme = theme;
        this.description = description;
        this.creator = creator;
        this.status = "PENDING";
        this.createdAt = LocalDateTime.now();
    }

    public Long getGroupId() { return groupId; }
    public String getName() { return name; }
    public void setName(String n) { this.name = n; }
    public String getTheme() { return theme; }
    public void setTheme(String t) { this.theme = t; }
    public String getDescription() { return description; }
    public void setDescription(String d) { this.description = d; }
    public String getStatus() { return status; }
    public void setStatus(String s) { this.status = s; }
    public User getCreator() { return creator; }
    public Book getFeaturedBook() { return featuredBook; }
    public void setFeaturedBook(Book b) { this.featuredBook = b; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getDecidedAt() { return decidedAt; }
    public void setDecidedAt(LocalDateTime t) { this.decidedAt = t; }
}