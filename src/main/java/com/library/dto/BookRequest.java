package com.library.dto;

import java.util.List;

public class BookRequest
{

    private String title;
    private String description;
    private Integer publicationYear;
    private boolean hasPhysicalCopy;
    private boolean hasDigitalCopy;
    private String digitalFilePath;

    private Long categoryId;
    private List<Long> authorIds;

    public String getTitle()
    {
        return title;
    }

    public void setTitle(String title)
    {
        this.title = title;
    }

    public String getDescription()
    {
        return description;
    }

    public void setDescription(String description)
    {
        this.description = description;
    }

    public Integer getPublicationYear()
    {
        return publicationYear;
    }

    public void setPublicationYear(Integer publicationYear)
    {
        this.publicationYear = publicationYear;
    }

    public boolean isHasPhysicalCopy()
    {
        return hasPhysicalCopy;
    }

    public void setHasPhysicalCopy(boolean hasPhysicalCopy)
    {
        this.hasPhysicalCopy = hasPhysicalCopy;
    }

    public boolean isHasDigitalCopy()
    {
        return hasDigitalCopy;
    }

    public void setHasDigitalCopy(boolean hasDigitalCopy)
    {
        this.hasDigitalCopy = hasDigitalCopy;
    }

    public String getDigitalFilePath()
    {
        return digitalFilePath;
    }

    public void setDigitalFilePath(String digitalFilePath)
    {
        this.digitalFilePath = digitalFilePath;
    }

    public Long getCategoryId()
    {
        return categoryId;
    }

    public void setCategoryId(Long categoryId)
    {
        this.categoryId = categoryId;
    }

    public List<Long> getAuthorIds()
    {
        return authorIds;
    }

    public void setAuthorIds(List<Long> authorIds)
    {
        this.authorIds = authorIds;
    }
}