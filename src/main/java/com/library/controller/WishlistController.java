package com.library.controller;

import com.library.dto.WishlistRequest;
import com.library.model.Book;
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

            List<Book> books = wishlist.stream()
                    .map(Wishlist::getBook)
                    .toList();

            return ResponseEntity.ok(books);
        }
        catch (IllegalArgumentException e)
        {
            return handleBadRequestException(e);
        }
        catch (RuntimeException e)
        {
            return handleNotFoundException(e);
        }
        catch (Exception e)
        {
            return handleGenericException(e);
        }
    }

    @PostMapping
    public ResponseEntity<?> addToWishlist(@RequestBody WishlistRequest request)
    {
        try
        {
            Wishlist createdWishlist = wishlistService.addToWishlist(request);

            return ResponseEntity.status(HttpStatus.CREATED).body(createdWishlist);
        }
        catch (IllegalArgumentException e)
        {
            return handleBadRequestException(e);
        }
        catch (IllegalStateException e)
        {
            return handleConflictException(e);
        }
        catch (RuntimeException e)
        {
            return handleNotFoundException(e);
        }
        catch (Exception e)
        {
            return handleGenericException(e);
        }
    }

    @DeleteMapping
    public ResponseEntity<?> removeFromWishlist(@RequestParam Long userId,
                                                @RequestParam Long bookId)
    {
        try
        {
            wishlistService.removeFromWishlist(userId, bookId);

            return ResponseEntity.noContent().build();
        }
        catch (IllegalArgumentException e)
        {
            return handleBadRequestException(e);
        }
        catch (RuntimeException e)
        {
            return handleNotFoundException(e);
        }
        catch (Exception e)
        {
            return handleGenericException(e);
        }
    }

    private ResponseEntity<String> handleBadRequestException(Exception e)
    {
        return ResponseEntity.badRequest().body(e.getMessage());
    }

    private ResponseEntity<String> handleNotFoundException(Exception e)
    {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
    }

    private ResponseEntity<String> handleConflictException(Exception e)
    {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
    }

    private ResponseEntity<String> handleGenericException(Exception e)
    {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Unexpected error: " + e.getMessage());
    }
}