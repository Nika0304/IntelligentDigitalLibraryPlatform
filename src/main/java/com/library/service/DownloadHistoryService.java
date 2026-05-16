package com.library.service;

import com.library.dto.DownloadRequest;
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

    public DownloadHistoryService(DownloadHistoryRepository downloadRepository,
                                  UserRepository userRepository,
                                  BookRepository bookRepository)
    {
        this.downloadRepository = downloadRepository;
        this.userRepository = userRepository;
        this.bookRepository = bookRepository;
    }

    @Transactional(readOnly = true)
    public List<DownloadHistory> getAllDownloads()
    {
        return downloadRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<DownloadHistory> getUserDownloads(Long userId)
    {
        User user = getUserById(userId);

        return downloadRepository.findByUserOrderByDownloadDateDesc(user);
    }

    @Transactional(readOnly = true)
    public List<DownloadHistory> getBookDownloads(Long bookId)
    {
        Book book = getBookById(bookId);

        return downloadRepository.findByBookOrderByDownloadDateDesc(book);
    }

    @Transactional
    public DownloadHistory recordDownload(DownloadRequest request)
    {
        validateDownloadRequest(request);

        User user = getUserById(request.getUserId());
        Book book = getBookById(request.getBookId());

        if (!book.isHasDigitalCopy())
        {
            throw new IllegalStateException("Book does not have a digital copy");
        }

        if (book.getDigitalFilePath() == null || book.getDigitalFilePath().trim().isEmpty())
        {
            throw new IllegalStateException("Book digital file path is missing");
        }

        DownloadHistory downloadHistory = new DownloadHistory(user, book);

        return downloadRepository.save(downloadHistory);
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

    private void validateDownloadRequest(DownloadRequest request)
    {
        if (request == null)
        {
            throw new IllegalArgumentException("Download request is required");
        }

        validateId(request.getUserId(), "User id");
        validateId(request.getBookId(), "Book id");
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