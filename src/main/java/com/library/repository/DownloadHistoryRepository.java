package com.library.repository;

import com.library.model.Book;
import com.library.model.DownloadHistory;
import com.library.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DownloadHistoryRepository extends JpaRepository<DownloadHistory, Long>
{
    List<DownloadHistory> findByUserOrderByDownloadDateDesc(User user);

    List<DownloadHistory> findByBookOrderByDownloadDateDesc(Book book);
}