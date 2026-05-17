package com.library.service;

import com.library.dto.BookRequest;
import com.library.dto.BookResponse;
import com.library.model.Author;
import com.library.model.Book;
import com.library.model.BookCopyStatus;
import com.library.model.Category;
import com.library.repository.AuthorRepository;
import com.library.repository.BookCopyRepository;
import com.library.repository.BookRepository;
import com.library.repository.CategoryRepository;
import com.library.repository.ReviewRepository;
import com.library.repository.ReservationRepository;

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
    private final BookCopyRepository bookCopyRepository;
    private final ReviewRepository reviewRepository;
    private final ReservationRepository reservationRepository;

    public BookService(BookRepository bookRepository,
                       CategoryRepository categoryRepository,
                       AuthorRepository authorRepository,
                       BookCopyRepository bookCopyRepository,
                       ReviewRepository reviewRepository,
                       ReservationRepository reservationRepository)
    {
        this.bookRepository = bookRepository;
        this.categoryRepository = categoryRepository;
        this.authorRepository = authorRepository;
        this.bookCopyRepository = bookCopyRepository;
        this.reviewRepository = reviewRepository;
        this.reservationRepository = reservationRepository;
    }

    public List<Book> getAllBooks()
    {
        return bookRepository.findAll();
    }

    public List<BookResponse> getAllBooksResponse()
    {
        return bookRepository.findAll().stream().map(this::toResponse).toList();
    }

    public BookResponse getBookResponseById(Long id)
    {
        return toResponse(getBookById(id));
    }

    public List<BookResponse> searchBooksByTitleResponse(String title)
    {
        return searchBooksByTitle(title).stream().map(this::toResponse).toList();
    }

    public List<BookResponse> getBooksByCategoryResponse(Long categoryId)
    {
        return getBooksByCategory(categoryId).stream().map(this::toResponse).toList();
    }

    public List<BookResponse> getDigitalBooksResponse()
    {
        return getDigitalBooks().stream().map(this::toResponse).toList();
    }

    public List<BookResponse> getPhysicalBooksResponse()
    {
        return getPhysicalBooks().stream().map(this::toResponse).toList();
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

    public BookResponse createBook(BookRequest request)
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
        book.setCoverImageURL(cleanText(request.getCoverImageURL()));

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

        return toResponse(bookRepository.save(book));
    }

    public BookResponse updateBook(Long id, BookRequest request)
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
        existingBook.setCoverImageURL(cleanText(request.getCoverImageURL()));

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

        return toResponse(bookRepository.save(existingBook));
    }

    public void deleteBook(Long id)
    {
        validateId(id);

        Book book = getBookById(id);

        if (reservationRepository.findByBook(book) != null
                && !reservationRepository.findByBook(book).isEmpty())
        {
            throw new IllegalStateException("Book cannot be deleted because it has reservation history");
        }

        bookCopyRepository.deleteAll(bookCopyRepository.findByBook(book));

        bookRepository.delete(book);
    }

    /** Centralized mapper used by all *Response endpoints. */
    public BookResponse toResponse(Book book)
    {
        int totalCopies = bookCopyRepository.findByBook(book).size();
        int availableCopies = bookCopyRepository.findByBookAndStatus(book, BookCopyStatus.AVAILABLE).size();

        var reviews = reviewRepository.findByBook(book);
        double avg = reviews.isEmpty() ? 0
                : Math.round(reviews.stream().mapToInt(r -> r.getRating()).average().orElse(0) * 10.0) / 10.0;

        return new BookResponse(
                book.getBookId(),
                book.getTitle(),
                book.getDescription(),
                book.getPublicationYear(),
                book.isHasPhysicalCopy(),
                book.isHasDigitalCopy(),
                book.getDigitalFilePath(),
                book.getCoverImageURL(),
                book.getCategory() != null ? book.getCategory().getCategoryId() : null,
                book.getCategory() != null ? book.getCategory().getName() : null,
                book.getAuthors() != null
                        ? book.getAuthors().stream().map(Author::getAuthorId).toList()
                        : List.of(),
                book.getAuthors() != null
                        ? book.getAuthors().stream().map(Author::getName).toList()
                        : List.of(),
                totalCopies,
                availableCopies,
                avg,
                reviews.size()
        );
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
