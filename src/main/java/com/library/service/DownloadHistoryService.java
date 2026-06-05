package com.library.service;

import com.library.dto.DownloadHistoryResponse;
import com.library.model.Book;
import com.library.model.DownloadHistory;
import com.library.model.User;
import com.library.repository.BookRepository;
import com.library.repository.DownloadHistoryRepository;
import com.library.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class DownloadHistoryService
{
    private final DownloadHistoryRepository downloadRepository;
    private final UserRepository userRepository;
    private final BookRepository bookRepository;
    private final BookService bookService;

    public DownloadHistoryService(DownloadHistoryRepository downloadRepository,
                                  UserRepository userRepository,
                                  BookRepository bookRepository,
                                  BookService bookService)
    {
        this.downloadRepository = downloadRepository;
        this.userRepository = userRepository;
        this.bookRepository = bookRepository;
        this.bookService = bookService;
    }

    @Transactional(readOnly = true)
    public List<DownloadHistoryResponse> getAllDownloads()
    {
        return downloadRepository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<DownloadHistoryResponse> getUserDownloads(Long userId)
    {
        User user = getUserById(userId);

        return downloadRepository.findByUserOrderByDownloadDateDesc(user)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<DownloadHistoryResponse> getBookDownloads(Long bookId)
    {
        Book book = getBookById(bookId);

        return downloadRepository.findByBookOrderByDownloadDateDesc(book)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public DownloadHistory saveDownload(DownloadHistory entry)
    {
        return downloadRepository.save(entry);
    }

    public DownloadHistoryResponse toResponse(DownloadHistory d)
    {
        return new DownloadHistoryResponse(
                d.getDownloadId(),
                d.getDownloadDate(),
                bookService.toResponse(d.getBook())
        );
    }

    private User getUserById(Long userId)
    {
        validateId(userId, "User id");

        return userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));
    }

    private Book getBookById(Long bookId)
    {
        validateId(bookId, "Book id");

        return bookRepository.findById(bookId)
                .orElseThrow(() -> new RuntimeException("Book not found with id: " + bookId));
    }
    private void validateId(Long id, String fieldName)
    {
        if (id == null)
        {
            throw new IllegalArgumentException(fieldName + " is required");
        }

        if (id <= 0)
        {
            throw new IllegalArgumentException(fieldName + " must be greater than 0");
        }
    }
}