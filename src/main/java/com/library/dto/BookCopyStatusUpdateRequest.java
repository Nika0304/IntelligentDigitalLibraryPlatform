package com.library.dto;

import com.library.model.BookCopyStatus;

public class BookCopyStatusUpdateRequest
{
    private BookCopyStatus status;

    public BookCopyStatusUpdateRequest() {}

    public BookCopyStatus getStatus()
    {
        return status;
    }

    public void setStatus(BookCopyStatus status)
    {
        this.status = status;
    }
}