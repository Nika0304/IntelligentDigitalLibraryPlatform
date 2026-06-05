package com.library.dto;

import java.time.LocalDateTime;

public class DownloadHistoryResponse
{
    private Long downloadId;
    private LocalDateTime downloadDate;
    private BookResponse book;

    public DownloadHistoryResponse(Long downloadId, LocalDateTime downloadDate, BookResponse book)
    {
        this.downloadId = downloadId;
        this.downloadDate = downloadDate;
        this.book = book;
    }

    public Long getDownloadId() { return downloadId; }
    public LocalDateTime getDownloadDate() { return downloadDate; }
    public BookResponse getBook() { return book; }
}