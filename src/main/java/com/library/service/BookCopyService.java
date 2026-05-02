package com.library.service;

import com.library.dto.BookCopyRequest;
import com.library.model.Book;
import com.library.model.BookCopy;
import com.library.model.BookCopyStatus;
import com.library.repository.BookCopyRepository;
import com.library.repository.BookRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BookCopyService
{

    private final BookCopyRepository bookCopyRepository;
    private final BookRepository bookRepository;

    public BookCopyService(BookCopyRepository bookCopyRepository, BookRepository bookRepository)
    {
        this.bookCopyRepository = bookCopyRepository;
        this.bookRepository = bookRepository;
    }

    public List<BookCopy> getAllBookCopies()
    {
        return bookCopyRepository.findAll();
    }

    public BookCopy getBookCopyById(Long copyId)
    {
        validateId(copyId);

        return bookCopyRepository.findById(copyId)
                .orElseThrow(() -> new RuntimeException("Book copy not found with id: " + copyId));
    }

    public List<BookCopy> getCopiesByBookId(Long bookId)
    {
        Book book = getBookById(bookId);

        return bookCopyRepository.findByBook(book);
    }

    public List<BookCopy> getAvailableCopiesByBookId(Long bookId)
    {
        Book book = getBookById(bookId);

        return bookCopyRepository.findByBookAndStatus(book, BookCopyStatus.AVAILABLE);
    }

    public BookCopy createBookCopy(BookCopyRequest request)
    {
        validateBookCopyRequest(request);

        String inventoryCode = request.getInventoryCode().trim();

        if (bookCopyRepository.existsByInventoryCode(inventoryCode))
        {
            throw new RuntimeException("Inventory code already exists: " + inventoryCode);
        }

        Book book = getBookById(request.getBookId());

        BookCopy bookCopy = new BookCopy();
        bookCopy.setInventoryCode(inventoryCode);
        bookCopy.setBook(book);
        bookCopy.setStatus(BookCopyStatus.AVAILABLE);

        book.setHasPhysicalCopy(true);
        bookRepository.save(book);

        return bookCopyRepository.save(bookCopy);
    }

    public BookCopy updateBookCopyStatus(Long copyId, BookCopyStatus status)
    {
        validateId(copyId);

        if (status == null)
        {
            throw new RuntimeException("Status is required");
        }

        BookCopy bookCopy = getBookCopyById(copyId);

        bookCopy.setStatus(status);

        return bookCopyRepository.save(bookCopy);
    }

    public void deleteBookCopy(Long copyId)
    {
        validateId(copyId);

        BookCopy bookCopy = getBookCopyById(copyId);

        if (bookCopy.getStatus() == BookCopyStatus.BORROWED)
        {
            throw new RuntimeException("Book copy cannot be deleted because it is currently borrowed");
        }

        if (bookCopy.getStatus() == BookCopyStatus.RESERVED ||
                bookCopy.getStatus() == BookCopyStatus.READY_FOR_PICKUP)
        {
            throw new RuntimeException("Book copy cannot be deleted because it is reserved");
        }

        Book book = bookCopy.getBook();

        bookCopyRepository.delete(bookCopy);

        List<BookCopy> remainingCopies = bookCopyRepository.findByBook(book);

        if (remainingCopies.isEmpty())
        {
            book.setHasPhysicalCopy(false);
            bookRepository.save(book);
        }
    }

    private Book getBookById(Long bookId)
    {
        validateId(bookId);

        return bookRepository.findById(bookId)
                .orElseThrow(() -> new RuntimeException("Book not found with id: " + bookId));
    }

    private void validateBookCopyRequest(BookCopyRequest request)
    {
        if (request == null)
        {
            throw new RuntimeException("Book copy request cannot be null");
        }

        if (request.getInventoryCode() == null || request.getInventoryCode().trim().isEmpty())
        {
            throw new RuntimeException("Inventory code is required");
        }

        if (request.getBookId() == null)
        {
            throw new RuntimeException("Book id is required");
        }

        validateId(request.getBookId());
    }

    private void validateId(Long id)
    {
        if (id == null || id <= 0)
        {
            throw new RuntimeException("Invalid id");
        }
    }
}