package com.library.controller;

import com.library.dto.WishlistRequest;
import com.library.model.Wishlist;
import com.library.service.WishlistService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/wishlist")
public class WishlistController
{
    private final WishlistService wishlistService;

    public WishlistController(WishlistService wishlistService)
    {
        this.wishlistService = wishlistService;
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<?> getUserWishlist(@PathVariable Long userId)
    {
        try
        {
            List<Wishlist> wishlist = wishlistService.getUserWishlist(userId);
            // Return as list of books (frontend expects BookResponse-like array)
            List<?> books = wishlist.stream().map(Wishlist::getBook).toList();
            return ResponseEntity.ok(books);
        }
        catch (RuntimeException e)
        {
            return handleException(e);
        }
    }

    @PostMapping
    public ResponseEntity<?> addToWishlist(@RequestBody WishlistRequest request)
    {
        try
        {
            Wishlist created = wishlistService.addToWishlist(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(created);
        }
        catch (RuntimeException e)
        {
            return handleException(e);
        }
    }

    @DeleteMapping
    public ResponseEntity<?> removeFromWishlist(@RequestParam Long userId, @RequestParam Long bookId)
    {
        try
        {
            wishlistService.removeFromWishlist(userId, bookId);
            return ResponseEntity.noContent().build();
        }
        catch (RuntimeException e)
        {
            return handleException(e);
        }
    }

    private ResponseEntity<String> handleException(RuntimeException e)
    {
        String message = e.getMessage();

        if (message != null && message.toLowerCase().contains("not found"))
        {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(message);
        }

        if (message != null && message.toLowerCase().contains("already"))
        {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(message);
        }

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(message);
    }
}
