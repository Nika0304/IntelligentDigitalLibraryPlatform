package com.library.dto;

import java.util.List;

public class BookResponse
{
    private Long bookId;
    private String title;
    private String description;
    private Integer publicationYear;
    private boolean hasPhysicalCopy;
    private boolean hasDigitalCopy;
    private String digitalFilePath;
    private String coverImageURL;

    private Long categoryId;
    private String categoryName;

    private List<Long> authorIds;
    private List<String> authors;

    private int totalCopies;
    private int availableCopies;

    private double averageRating;
    private int reviewCount;

    public BookResponse() {}

    public BookResponse(Long bookId, String title, String description, Integer publicationYear,
                        boolean hasPhysicalCopy, boolean hasDigitalCopy, String digitalFilePath,
                        String coverImageURL, Long categoryId, String categoryName,
                        List<Long> authorIds, List<String> authors,
                        int totalCopies, int availableCopies,
                        double averageRating, int reviewCount)
    {
        this.bookId = bookId;
        this.title = title;
        this.description = description;
        this.publicationYear = publicationYear;
        this.hasPhysicalCopy = hasPhysicalCopy;
        this.hasDigitalCopy = hasDigitalCopy;
        this.digitalFilePath = digitalFilePath;
        this.coverImageURL = coverImageURL;
        this.categoryId = categoryId;
        this.categoryName = categoryName;
        this.authorIds = authorIds;
        this.authors = authors;
        this.totalCopies = totalCopies;
        this.availableCopies = availableCopies;
        this.averageRating = averageRating;
        this.reviewCount = reviewCount;
    }

    public Long getBookId() { return bookId; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public Integer getPublicationYear() { return publicationYear; }
    public boolean isHasPhysicalCopy() { return hasPhysicalCopy; }
    public boolean isHasDigitalCopy() { return hasDigitalCopy; }
    public String getDigitalFilePath() { return digitalFilePath; }
    public String getCoverImageURL() { return coverImageURL; }
    public Long getCategoryId() { return categoryId; }
    public String getCategoryName() { return categoryName; }
    public List<Long> getAuthorIds() { return authorIds; }
    public List<String> getAuthors() { return authors; }
    public int getTotalCopies() { return totalCopies; }
    public int getAvailableCopies() { return availableCopies; }
    public double getAverageRating() { return averageRating; }
    public int getReviewCount() { return reviewCount; }
}
