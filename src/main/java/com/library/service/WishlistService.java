package com.library.service;

import com.library.dto.WishlistRequest;
import com.library.model.Book;
import com.library.model.User;
import com.library.model.Wishlist;
import com.library.repository.BookRepository;
import com.library.repository.UserRepository;
import com.library.repository.WishlistRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class WishlistService
{
    private final WishlistRepository wishlistRepository;
    private final UserRepository userRepository;
    private final BookRepository bookRepository;

    public WishlistService(WishlistRepository wishlistRepository,
                           UserRepository userRepository,
                           BookRepository bookRepository)
    {
        this.wishlistRepository = wishlistRepository;
        this.userRepository = userRepository;
        this.bookRepository = bookRepository;
    }

    @Transactional(readOnly = true)
    public List<Wishlist> getUserWishlist(Long userId)
    {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));
        return wishlistRepository.findByUser(user);
    }

    @Transactional
    public Wishlist addToWishlist(WishlistRequest request)
    {
        if (request == null || request.getUserId() == null || request.getBookId() == null)
        {
            throw new RuntimeException("User id and book id are required");
        }

        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found with id: " + request.getUserId()));

        Book book = bookRepository.findById(request.getBookId())
                .orElseThrow(() -> new RuntimeException("Book not found with id: " + request.getBookId()));

        if (wishlistRepository.existsByUserAndBook(user, book))
        {
            throw new RuntimeException("Book already in wishlist");
        }

        return wishlistRepository.save(new Wishlist(user, book));
    }

    @Transactional
    public void removeFromWishlist(Long userId, Long bookId)
    {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new RuntimeException("Book not found"));
        wishlistRepository.deleteByUserAndBook(user, book);
    }
}
