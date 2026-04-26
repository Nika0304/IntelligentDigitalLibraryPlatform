package com.library.dto;

import java.util.List;

public class BookResponse
{

    private Long bookId;
    private String title;
    private String categoryName;
    private List<String> authors;

    public BookResponse(Long bookId, String title, String categoryName, List<String> authors)
    {
        this.bookId = bookId;
        this.title = title;
        this.categoryName = categoryName;
        this.authors = authors;
    }

    public Long getBookId()
    {
        return bookId;
    }

    public String getTitle()
    {
        return title;
    }

    public String getCategoryName()
    {
        return categoryName;
    }

    public List<String> getAuthors()
    {
        return authors;
    }
}