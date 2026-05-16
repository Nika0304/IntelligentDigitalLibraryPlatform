package com.library.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "wishlist", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"user_id", "book_id"})
})
public class Wishlist
{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long wishlistId;

    @ManyToOne(optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    @JsonIgnore
    private User user;

    @ManyToOne(optional = false)
    @JoinColumn(name = "book_id", nullable = false)
    private Book book;

    @Column(nullable = false, updatable = false)
    private LocalDateTime addedAt;

    public Wishlist() {}

    public Wishlist(User user, Book book)
    {
        this.user = user;
        this.book = book;
        this.addedAt = LocalDateTime.now();
    }

    @PrePersist
    protected void onCreate()
    {
        if (addedAt == null)
        {
            addedAt = LocalDateTime.now();
        }
    }

    public Long getWishlistId() { return wishlistId; }
    public void setWishlistId(Long wishlistId) { this.wishlistId = wishlistId; }
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
    public Book getBook() { return book; }
    public void setBook(Book book) { this.book = book; }
    public LocalDateTime getAddedAt() { return addedAt; }
    public void setAddedAt(LocalDateTime addedAt) { this.addedAt = addedAt; }
}
