package com.library.repository;

import com.library.model.User;
import com.library.model.Book;
import com.library.model.Review;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ReviewRepository extends JpaRepository<Review, Long> {
    List<Review> findByBook(Book book);
    List<Review> findByUser(User user);
    List<Review> findByUserAndBook(User user, Book book);
}
