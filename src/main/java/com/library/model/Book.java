package com.library.model;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Table(name = "books")

public class Book {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long bookId;

    @Column(nullable = false)
    private String title;

    @Column(length = 2000)
    private String description;

    private Integer publicationYear;
    private boolean hasPhysicalCopy;
    private boolean hasDigitalCopy;
    private String digitalFilePath;

    // NEW: URL public al copertei (Unsplash, Google Books etc.)
    @Column(length = 500)
    private String coverImageURL;

    @ManyToOne
    @JoinColumn(name = "category_id")
    private Category category;

    @ManyToMany
    @JoinTable(
            name = "book_authors",
            joinColumns = @JoinColumn(name = "book_id"),
            inverseJoinColumns = @JoinColumn(name = "author_id")
    )
    private List<Author> authors = new ArrayList<>();

    @JsonIgnore
    @OneToMany(mappedBy = "book")
    private List<BookCopy> copies = new ArrayList<>();

    public Book(){}
    public Book(String title, String description, Integer publicationYear, Category category){
        this.title = title;
        this.description = description;
        this.publicationYear = publicationYear;
        this.category = category;
    }

    public Long getBookId(){ return bookId; }
    public void setBookId(Long bookId){ this.bookId = bookId; }

    public String getTitle(){ return title; }
    public void setTitle(String title){ this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Integer getPublicationYear() { return publicationYear; }
    public void setPublicationYear(Integer publicationYear) { this.publicationYear = publicationYear; }

    public boolean isHasPhysicalCopy() { return hasPhysicalCopy; }
    public void setHasPhysicalCopy(boolean hasPhysicalCopy) { this.hasPhysicalCopy = hasPhysicalCopy; }

    public boolean isHasDigitalCopy() { return hasDigitalCopy; }
    public void setHasDigitalCopy(boolean hasDigitalCopy) { this.hasDigitalCopy = hasDigitalCopy; }

    public String getDigitalFilePath() { return digitalFilePath; }
    public void setDigitalFilePath(String digitalFilePath) { this.digitalFilePath = digitalFilePath; }

    // NEW
    public String getCoverImageURL() { return coverImageURL; }
    public void setCoverImageURL(String coverImageURL) { this.coverImageURL = coverImageURL; }

    public Category getCategory() { return category; }
    public void setCategory(Category category) { this.category = category; }

    public List<Author> getAuthors() { return authors; }
    public void setAuthors(List<Author> authors) { this.authors = authors; }

    public List<BookCopy> getCopies() { return copies; }
    public void setCopies(List<BookCopy> copies) { this.copies = copies; }
}
