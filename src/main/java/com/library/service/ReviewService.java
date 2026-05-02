package com.library.service;

import com.library.dto.ReviewRequest;
import com.library.model.Book;
import com.library.model.Review;
import com.library.model.User;
import com.library.model.UserStatus;
import com.library.repository.BookRepository;
import com.library.repository.ReviewRepository;
import com.library.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ReviewService
{

    private final ReviewRepository reviewRepository;
    private final UserRepository userRepository;
    private final BookRepository bookRepository;

    public ReviewService(ReviewRepository reviewRepository,UserRepository userRepository, BookRepository bookRepository)
    {
        this.reviewRepository = reviewRepository;
        this.userRepository = userRepository;
        this.bookRepository = bookRepository;
    }

    public List<Review> getAllReviews()
    {
        return reviewRepository.findAll();
    }

    public Review getReviewById(Long reviewId)
    {
        validateId(reviewId, "Review id");

        return reviewRepository.findById(reviewId)
                .orElseThrow(() -> new RuntimeException("Review not found with id: " + reviewId));
    }

    public List<Review> getReviewsByBookId(Long bookId)
    {
        validateId(bookId, "Book id");

        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new RuntimeException("Book not found with id: " + bookId));

        return reviewRepository.findByBook(book);
    }

    public List<Review> getReviewsByUserId(Long userId)
    {
        validateId(userId, "User id");

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));

        return reviewRepository.findByUser(user);
    }

    public Review createReview(ReviewRequest request)
    {
        validateReviewRequestForCreate(request);

        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found with id: " + request.getUserId()));

        Book book = bookRepository.findById(request.getBookId())
                .orElseThrow(() -> new RuntimeException("Book not found with id: " + request.getBookId()));

        validateUserCanCreateReview(user);

        Review review = new Review();
        review.setUser(user);
        review.setBook(book);
        review.setRating(request.getRating());
        review.setComment(normalizeComment(request.getComment()));

        return reviewRepository.save(review);
    }

    public Review updateReview(Long reviewId, ReviewRequest request)
    {
        validateId(reviewId, "Review id");
        validateReviewRequestForUpdate(request);

        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new RuntimeException("Review not found with id: " + reviewId));

        validateUserCanCreateReview(review.getUser());

        review.setRating(request.getRating());
        review.setComment(normalizeComment(request.getComment()));

        return reviewRepository.save(review);
    }

    public void deleteReview(Long reviewId)
    {
        validateId(reviewId, "Review id");

        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new RuntimeException("Review not found with id: " + reviewId));

        reviewRepository.delete(review);
    }

    private void validateReviewRequestForCreate(ReviewRequest request)
    {
        if (request == null)
        {
            throw new IllegalArgumentException("Review request is required");
        }

        validateId(request.getUserId(), "User id");
        validateId(request.getBookId(), "Book id");
        validateRating(request.getRating());
        validateComment(request.getComment());
    }

    private void validateReviewRequestForUpdate(ReviewRequest request)
    {
        if (request == null)
        {
            throw new IllegalArgumentException("Review request is required");
        }

        validateRating(request.getRating());
        validateComment(request.getComment());
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

    private void validateRating(int rating)
    {
        if (rating < 1 || rating > 5)
        {
            throw new IllegalArgumentException("Rating must be between 1 and 5");
        }
    }

    private void validateComment(String comment)
    {
        if (comment != null && comment.length() > 1000)
        {
            throw new IllegalArgumentException("Comment cannot exceed 1000 characters");
        }
    }

    private void validateUserCanCreateReview(User user)
    {
        if (user.getStatus() != UserStatus.ACTIVE)
        {
            throw new IllegalStateException("Only active users can create reviews");
        }
    }

    private String normalizeComment(String comment)
    {
        if (comment == null)
        {
            return null;
        }

        String trimmedComment = comment.trim();

        if (trimmedComment.isEmpty())
        {
            return null;
        }

        return trimmedComment;
    }
}