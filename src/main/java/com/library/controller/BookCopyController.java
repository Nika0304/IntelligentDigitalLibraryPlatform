package com.library.controller;

import com.library.dto.BookCopyRequest;
import com.library.dto.BookCopyStatusUpdateRequest;
import com.library.model.BookCopy;
import com.library.service.BookCopyService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/book-copies")
public class BookCopyController
{

    private final BookCopyService bookCopyService;

    public BookCopyController(BookCopyService bookCopyService)
    {
        this.bookCopyService = bookCopyService;
    }

    // get all book copies
    @GetMapping
    public ResponseEntity<List<BookCopy>> getAllBookCopies()
    {
        return ResponseEntity.ok(bookCopyService.getAllBookCopies());
    }

    // get book copy by id
    @GetMapping("/{copyId}")
    public ResponseEntity<?> getBookCopyById(@PathVariable Long copyId)
    {
        try
        {
            return ResponseEntity.ok(bookCopyService.getBookCopyById(copyId));
        }
        catch (RuntimeException e)
        {
            return handleBookCopyException(e);
        }
    }

    // get all copies for a book
    @GetMapping("/book/{bookId}")
    public ResponseEntity<?> getCopiesByBookId(@PathVariable Long bookId)
    {
        try
        {
            return ResponseEntity.ok(bookCopyService.getCopiesByBookId(bookId));
        }
        catch (RuntimeException e)
        {
            return handleBookCopyException(e);
        }
    }

    // get available copies for a book
    @GetMapping("/book/{bookId}/available")
    public ResponseEntity<?> getAvailableCopiesByBookId(@PathVariable Long bookId)
    {
        try
        {
            return ResponseEntity.ok(bookCopyService.getAvailableCopiesByBookId(bookId));
        }
        catch (RuntimeException e)
        {
            return handleBookCopyException(e);
        }
    }

    // create book copy
    @PostMapping
    public ResponseEntity<?> createBookCopy(@RequestBody BookCopyRequest request)
    {
        try
        {
            return ResponseEntity.status(HttpStatus.CREATED).body(bookCopyService.createBookCopy(request));
        }
        catch (RuntimeException e)
        {
            return handleBookCopyException(e);
        }
    }

    // update book copy status
    @PutMapping("/{copyId}/status")
    public ResponseEntity<?> updateBookCopyStatus(@PathVariable Long copyId,
                                                  @RequestBody BookCopyStatusUpdateRequest request)
    {
        try
        {
            if (request == null)
            {
                throw new RuntimeException("Status request cannot be null");
            }

            return ResponseEntity.ok(bookCopyService.updateBookCopyStatus(copyId, request.getStatus()));
        }
        catch (RuntimeException e)
        {
            return handleBookCopyException(e);
        }
    }

    // delete book copy
    @DeleteMapping("/{copyId}")
    public ResponseEntity<?> deleteBookCopy(@PathVariable Long copyId)
    {
        try
        {
            bookCopyService.deleteBookCopy(copyId);
            return ResponseEntity.noContent().build();
        }
        catch (RuntimeException e)
        {
            return handleBookCopyException(e);
        }
    }

    private ResponseEntity<String> handleBookCopyException(RuntimeException e)
    {
        String message = e.getMessage();

        if (message != null && message.toLowerCase().contains("not found"))
        {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(message);
        }

        if (message != null && (
                message.toLowerCase().contains("already exists") ||
                        message.toLowerCase().contains("duplicate")
        ))
        {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(message);
        }

        if (message != null && (
                message.toLowerCase().contains("borrowed") ||
                        message.toLowerCase().contains("reserved")
        ))
        {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(message);
        }

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(message);
    }
}