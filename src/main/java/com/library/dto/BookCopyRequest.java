package com.library.dto;

public class BookCopyRequest {

    private String inventoryCode;
    private Long bookId;

    public BookCopyRequest() {}

    public String getInventoryCode()
    {
        return inventoryCode;
    }

    public void setInventoryCode(String inventoryCode)
    {
        this.inventoryCode = inventoryCode;
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