package com.library.dto;

public class DownloadRequest
{
    private Long userId;
    private Long bookId;

    public DownloadRequest() {}

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public Long getBookId() { return bookId; }
    public void setBookId(Long bookId) { this.bookId = bookId; }
}
