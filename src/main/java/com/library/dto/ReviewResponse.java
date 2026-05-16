package com.library.dto;

import java.time.LocalDateTime;

public class ReviewResponse
{
    private Long reviewId;
    private int rating;
    private String comment;
    private LocalDateTime createdAt;
    private Long userId;
    private Long bookId;
    private String userName;

    public ReviewResponse() {}

    public ReviewResponse(Long reviewId, int rating, String comment, LocalDateTime createdAt,
                          Long userId, Long bookId, String userName)
    {
        this.reviewId = reviewId;
        this.rating = rating;
        this.comment = comment;
        this.createdAt = createdAt;
        this.userId = userId;
        this.bookId = bookId;
        this.userName = userName;
    }

    public Long getReviewId() { return reviewId; }
    public int getRating() { return rating; }
    public String getComment() { return comment; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public Long getUserId() { return userId; }
    public Long getBookId() { return bookId; }
    public String getUserName() { return userName; }
}
