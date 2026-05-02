package com.library.controller;

import com.library.dto.ReviewRequest;
import com.library.model.Review;
import com.library.service.ReviewService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reviews")
public class ReviewController
{

    private final ReviewService reviewService;

    public ReviewController(ReviewService reviewService)
    {
        this.reviewService = reviewService;
    }

    @GetMapping
    public ResponseEntity<?> getAllReviews()
    {
        try
        {
            List<Review> reviews = reviewService.getAllReviews();
            return ResponseEntity.ok(reviews);
        }
        catch (Exception e)
        {
            return handleGenericException(e);
        }
    }

    @GetMapping("/{reviewId}")
    public ResponseEntity<?> getReviewById(@PathVariable Long reviewId)
    {
        try
        {
            Review review = reviewService.getReviewById(reviewId);
            return ResponseEntity.ok(review);
        }
        catch (IllegalArgumentException e)
        {
            return handleBadRequestException(e);
        }
        catch (RuntimeException e)
        {
            return handleNotFoundException(e);
        }
    }

    @GetMapping("/book/{bookId}")
    public ResponseEntity<?> getReviewsByBookId(@PathVariable Long bookId)
    {
        try
        {
            List<Review> reviews = reviewService.getReviewsByBookId(bookId);
            return ResponseEntity.ok(reviews);
        }
        catch (IllegalArgumentException e)
        {
            return handleBadRequestException(e);
        }
        catch (RuntimeException e)
        {
            return handleNotFoundException(e);
        }
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<?> getReviewsByUserId(@PathVariable Long userId)
    {
        try
        {
            List<Review> reviews = reviewService.getReviewsByUserId(userId);
            return ResponseEntity.ok(reviews);
        }
        catch (IllegalArgumentException e)
        {
            return handleBadRequestException(e);
        }
        catch (RuntimeException e)
        {
            return handleNotFoundException(e);
        }
    }

    @PostMapping
    public ResponseEntity<?> createReview(@RequestBody ReviewRequest request)
    {
        try
        {
            Review createdReview = reviewService.createReview(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdReview);
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
    }

    @PutMapping("/{reviewId}")
    public ResponseEntity<?> updateReview(@PathVariable Long reviewId, @RequestBody ReviewRequest request)
    {
        try
        {
            Review updatedReview = reviewService.updateReview(reviewId, request);
            return ResponseEntity.ok(updatedReview);
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
    }

    @DeleteMapping("/{reviewId}")
    public ResponseEntity<?> deleteReview(@PathVariable Long reviewId)
    {
        try
        {
            reviewService.deleteReview(reviewId);
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
    }

    private ResponseEntity<String> handleBadRequestException(Exception e)
    {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
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
                .body("An unexpected error occurred");
    }
}