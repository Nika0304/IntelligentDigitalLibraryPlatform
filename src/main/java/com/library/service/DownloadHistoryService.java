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
    public List<DownloadHistory> getUserDownloads(Long userId)
    {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));
        return downloadRepository.findByUserOrderByDownloadDateDesc(user);
    }

    @Transactional
    public DownloadHistory recordDownload(DownloadRequest request)
    {
        if (request == null || request.getUserId() == null || request.getBookId() == null)
        {
            throw new RuntimeException("User id and book id are required");
        }

        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found with id: " + request.getUserId()));

        Book book = bookRepository.findById(request.getBookId())
                .orElseThrow(() -> new RuntimeException("Book not found with id: " + request.getBookId()));

        if (!book.isHasDigitalCopy())
        {
            throw new RuntimeException("Book does not have a digital copy");
        }

        return downloadRepository.save(new DownloadHistory(user, book));
    }
}
