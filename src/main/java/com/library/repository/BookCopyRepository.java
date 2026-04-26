package com.library.repository;

import com.library.model.Book;
import com.library.model.BookCopy;
import com.library.model.BookCopyStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface BookCopyRepository extends JpaRepository<BookCopy, Long> {
    List<BookCopy> findByBook(Book book);
    List<BookCopy> findByBookAndStatus(Book book, BookCopyStatus status);
    Optional<BookCopy> findFirstByBookAndStatus(Book book, BookCopyStatus status);
    boolean existsByInventoryCode(String inventoryCode);
}
