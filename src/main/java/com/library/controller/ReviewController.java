package com.library.controller;

import com.library.dto.ReviewRequest;
import com.library.dto.ReviewResponse;
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

    private ReviewResponse toResponse(Review r)
    {
        return new ReviewResponse(
                r.getReviewId(),
                r.getRating(),
                r.getComment(),
                r.getCreatedAt(),
                r.getUser() != null ? r.getUser().getUserId() : null,
                r.getBook() != null ? r.getBook().getBookId() : null,
                r.getUser() != null ? r.getUser().getFullName() : "Utilizator"
        );
    }

    @GetMapping
    public ResponseEntity<?> getAllReviews()
    {
        try
        {
            List<ReviewResponse> reviews = reviewService.getAllReviews().stream().map(this::toResponse).toList();
            return ResponseEntity.ok(reviews);
        }
        catch (Exception e)
        {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An unexpected error occurred");
        }
    }

    @GetMapping("/{reviewId}")
    public ResponseEntity<?> getReviewById(@PathVariable Long reviewId)
    {
        try
        {
            return ResponseEntity.ok(toResponse(reviewService.getReviewById(reviewId)));
        }
        catch (IllegalArgumentException e)
        {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
        catch (RuntimeException e)
        {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    @GetMapping("/book/{bookId}")
    public ResponseEntity<?> getReviewsByBookId(@PathVariable Long bookId)
    {
        try
        {
            List<ReviewResponse> reviews = reviewService.getReviewsByBookId(bookId).stream().map(this::toResponse).toList();
            return ResponseEntity.ok(reviews);
        }
        catch (IllegalArgumentException e)
        {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
        catch (RuntimeException e)
        {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<?> getReviewsByUserId(@PathVariable Long userId)
    {
        try
        {
            List<ReviewResponse> reviews = reviewService.getReviewsByUserId(userId).stream().map(this::toResponse).toList();
            return ResponseEntity.ok(reviews);
        }
        catch (IllegalArgumentException e)
        {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
        catch (RuntimeException e)
        {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    @PostMapping
    public ResponseEntity<?> createReview(@RequestBody ReviewRequest request)
    {
        try
        {
            Review created = reviewService.createReview(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(created));
        }
        catch (IllegalArgumentException e)
        {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
        catch (IllegalStateException e)
        {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
        }
        catch (RuntimeException e)
        {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    @PutMapping("/{reviewId}")
    public ResponseEntity<?> updateReview(@PathVariable Long reviewId, @RequestBody ReviewRequest request)
    {
        try
        {
            return ResponseEntity.ok(toResponse(reviewService.updateReview(reviewId, request)));
        }
        catch (IllegalArgumentException e)
        {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
        catch (IllegalStateException e)
        {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
        }
        catch (RuntimeException e)
        {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
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
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
        catch (RuntimeException e)
        {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }
}
