package com.library.repository;

import com.library.model.Book;
import com.library.model.User;
import com.library.model.Wishlist;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface WishlistRepository extends JpaRepository<Wishlist, Long>
{
    List<Wishlist> findByUser(User user);
    Optional<Wishlist> findByUserAndBook(User user, Book book);
    boolean existsByUserAndBook(User user, Book book);
    void deleteByUserAndBook(User user, Book book);
}
