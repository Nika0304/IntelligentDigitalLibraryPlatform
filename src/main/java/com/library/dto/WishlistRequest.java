package com.library.dto;

public class WishlistRequest
{
    private Long userId;
    private Long bookId;

    public WishlistRequest()
    {
    }

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