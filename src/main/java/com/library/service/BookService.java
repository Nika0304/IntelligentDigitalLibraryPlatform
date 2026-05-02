package com.library.service;

import com.library.dto.BookRequest;
import com.library.dto.BookResponse;
import com.library.model.Author;
import com.library.model.Book;
import com.library.model.Category;
import com.library.repository.AuthorRepository;
import com.library.repository.BookRepository;
import com.library.repository.CategoryRepository;
import org.springframework.stereotype.Service;

import java.time.Year;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class BookService
{

    private final BookRepository bookRepository;
    private final CategoryRepository categoryRepository;
    private final AuthorRepository authorRepository;

    public BookService(BookRepository bookRepository,
                       CategoryRepository categoryRepository,
                       AuthorRepository authorRepository)
    {
        this.bookRepository = bookRepository;
        this.categoryRepository = categoryRepository;
        this.authorRepository = authorRepository;
    }

    public List<Book> getAllBooks()
    {
        return bookRepository.findAll();
    }

    public List<BookResponse> getAllBooksResponse()
    {
        return bookRepository.findAll().stream()
                .map(book -> new BookResponse(
                        book.getBookId(),
                        book.getTitle(),
                        book.getCategory() != null ? book.getCategory().getName() : null,
                        book.getAuthors() != null
                                ? book.getAuthors().stream()
                                .map(author -> author.getName())
                                .toList()
                                : List.of()
                ))
                .toList();
    }

    public Book getBookById(Long id)
    {
        validateId(id);

        return bookRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Book not found with id: " + id));
    }

    public List<Book> searchBooksByTitle(String title)
    {
        if (title == null || title.trim().isEmpty())
        {
            return bookRepository.findAll();
        }

        return bookRepository.findByTitleContainingIgnoreCase(title.trim());
    }

    public List<Book> getBooksByCategory(Long categoryId)
    {
        Category category = getCategoryById(categoryId);
        return bookRepository.findByCategory(category);
    }

    public List<Book> getDigitalBooks()
    {
        return bookRepository.findByHasDigitalCopyTrue();
    }

    public List<Book> getPhysicalBooks()
    {
        return bookRepository.findByHasPhysicalCopyTrue();
    }

    public Book createBook(BookRequest request)
    {
        validateBookRequest(request);

        Category category = getCategoryById(request.getCategoryId());
        List<Author> authors = getAuthorsByIds(request.getAuthorIds());

        Book book = new Book();

        book.setTitle(request.getTitle().trim());
        book.setDescription(cleanText(request.getDescription()));
        book.setPublicationYear(request.getPublicationYear());
        book.setHasPhysicalCopy(request.isHasPhysicalCopy());
        book.setHasDigitalCopy(request.isHasDigitalCopy());

        if (request.isHasDigitalCopy())
        {
            book.setDigitalFilePath(request.getDigitalFilePath().trim());
        }
        else
        {
            book.setDigitalFilePath(null);
        }

        book.setCategory(category);
        book.setAuthors(authors);

        return bookRepository.save(book);
    }

    public Book updateBook(Long id, BookRequest request)
    {
        validateId(id);
        validateBookRequest(request);

        Book existingBook = getBookById(id);

        Category category = getCategoryById(request.getCategoryId());
        List<Author> authors = getAuthorsByIds(request.getAuthorIds());

        existingBook.setTitle(request.getTitle().trim());
        existingBook.setDescription(cleanText(request.getDescription()));
        existingBook.setPublicationYear(request.getPublicationYear());
        existingBook.setHasPhysicalCopy(request.isHasPhysicalCopy());
        existingBook.setHasDigitalCopy(request.isHasDigitalCopy());

        if (request.isHasDigitalCopy())
        {
            existingBook.setDigitalFilePath(request.getDigitalFilePath().trim());
        }
        else
        {
            existingBook.setDigitalFilePath(null);
        }

        existingBook.setCategory(category);
        existingBook.setAuthors(authors);

        return bookRepository.save(existingBook);
    }

    public void deleteBook(Long id)
    {
        validateId(id);

        Book book = getBookById(id);

        if (book.getCopies() != null && !book.getCopies().isEmpty())
        {
            throw new RuntimeException("Book cannot be deleted because it has physical copies");
        }

        bookRepository.delete(book);
    }

    private Category getCategoryById(Long categoryId)
    {
        validateId(categoryId);

        return categoryRepository.findById(categoryId)
                .orElseThrow(() -> new RuntimeException("Category not found with id: " + categoryId));
    }

    private List<Author> getAuthorsByIds(List<Long> authorIds)
    {
        if (authorIds == null || authorIds.isEmpty())
        {
            throw new RuntimeException("At least one author is required");
        }

        for (Long authorId : authorIds)
        {
            validateId(authorId);
        }

        Set<Long> uniqueAuthorIds = new HashSet<>(authorIds);

        if (uniqueAuthorIds.size() != authorIds.size())
        {
            throw new RuntimeException("Duplicate author ids are not allowed");
        }

        List<Author> authors = authorRepository.findAllById(authorIds);

        if (authors.size() != authorIds.size())
        {
            throw new RuntimeException("One or more authors were not found");
        }

        return authors;
    }

    private void validateBookRequest(BookRequest request)
    {
        if (request == null)
        {
            throw new RuntimeException("Book request cannot be null");
        }

        if (request.getTitle() == null || request.getTitle().trim().isEmpty())
        {
            throw new RuntimeException("Book title cannot be empty");
        }

        if (request.getCategoryId() == null)
        {
            throw new RuntimeException("Category is required");
        }

        if (request.getAuthorIds() == null || request.getAuthorIds().isEmpty())
        {
            throw new RuntimeException("At least one author is required");
        }

        if (!request.isHasPhysicalCopy() && !request.isHasDigitalCopy())
        {
            throw new RuntimeException("Book must have at least one format: physical or digital");
        }

        if (request.getPublicationYear() != null)
        {
            int currentYear = Year.now().getValue();

            if (request.getPublicationYear() < 0)
            {
                throw new RuntimeException("Publication year cannot be negative");
            }

            if (request.getPublicationYear() > currentYear)
            {
                throw new RuntimeException("Publication year cannot be in the future");
            }
        }

        if (request.isHasDigitalCopy()
                && (request.getDigitalFilePath() == null || request.getDigitalFilePath().trim().isEmpty()))
        {
            throw new RuntimeException("Digital file path is required for digital books");
        }
    }

    private void validateId(Long id)
    {
        if (id == null || id <= 0)
        {
            throw new RuntimeException("Invalid id");
        }
    }

    private String cleanText(String text)
    {
        if (text == null || text.trim().isEmpty())
        {
            return null;
        }

        return text.trim();
    }
}