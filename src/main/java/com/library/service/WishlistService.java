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
        User user = getUserById(userId);

        return wishlistRepository.findByUser(user);
    }

    @Transactional
    public Wishlist addToWishlist(WishlistRequest request)
    {
        validateWishlistRequest(request);

        User user = getUserById(request.getUserId());
        Book book = getBookById(request.getBookId());

        if (wishlistRepository.existsByUserAndBook(user, book))
        {
            throw new IllegalStateException("Book already in wishlist");
        }

        Wishlist wishlist = new Wishlist(user, book);

        return wishlistRepository.save(wishlist);
    }

    @Transactional
    public void removeFromWishlist(Long userId, Long bookId)
    {
        User user = getUserById(userId);
        Book book = getBookById(bookId);

        if (!wishlistRepository.existsByUserAndBook(user, book))
        {
            throw new RuntimeException("Wishlist item not found");
        }

        wishlistRepository.deleteByUserAndBook(user, book);
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

    private void validateWishlistRequest(WishlistRequest request)
    {
        if (request == null)
        {
            throw new IllegalArgumentException("Wishlist request is required");
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