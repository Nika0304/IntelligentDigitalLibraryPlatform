package com.library.dto;

public class ReservationRequest
{
    private Long userId;
    private Long bookId;

    public ReservationRequest() {}

    public Long getUserId()
    {
        return userId;
    }

    public void setUserId(Long userId)
    {
        this.userId = userId;
    }

    public Long getBookId()
    {
        return bookId;
    }

    public void setBookId(Long bookId)
    {
        this.bookId = bookId;
    }
}