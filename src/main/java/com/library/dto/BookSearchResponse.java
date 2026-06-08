package com.library.dto;

import java.util.List;
import java.util.Map;

public class BookSearchResponse
{
    private List<BookResponse> items;
    private long total;
    private int page;
    private int size;
    private int totalPages;
    private Map<String, Object> facets;

    public BookSearchResponse() {}

    public BookSearchResponse(List<BookResponse> items, long total, int page, int size,
                              int totalPages, Map<String, Object> facets)
    {
        this.items = items;
        this.total = total;
        this.page = page;
        this.size = size;
        this.totalPages = totalPages;
        this.facets = facets;
    }

    public List<BookResponse> getItems() { return items; }
    public void setItems(List<BookResponse> items) { this.items = items; }

    public long getTotal() { return total; }
    public void setTotal(long total) { this.total = total; }

    public int getPage() { return page; }
    public void setPage(int page) { this.page = page; }

    public int getSize() { return size; }
    public void setSize(int size) { this.size = size; }

    public int getTotalPages() { return totalPages; }
    public void setTotalPages(int totalPages) { this.totalPages = totalPages; }

    public Map<String, Object> getFacets() { return facets; }
    public void setFacets(Map<String, Object> facets) { this.facets = facets; }
}