package com.library.controller;

import com.library.dto.BookRequest;
import com.library.dto.BookResponse;
import com.library.service.BookService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/books")
public class BookController
{
    private final BookService bookService;

    public BookController(BookService bookService)
    {
        this.bookService = bookService;
    }

    @GetMapping
    public ResponseEntity<List<BookResponse>> getAllBooks()
    {
        return ResponseEntity.ok(bookService.getAllBooksResponse());
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getBookById(@PathVariable Long id)
    {
        try
        {
            return ResponseEntity.ok(bookService.getBookResponseById(id));
        }
        catch (RuntimeException e)
        {
            return handleBookException(e);
        }
    }

    @GetMapping("/search")
    public ResponseEntity<?> searchBooks(@RequestParam String title)
    {
        try
        {
            return ResponseEntity.ok(bookService.searchBooksByTitleResponse(title));
        }
        catch (RuntimeException e)
        {
            return handleBookException(e);
        }
    }

    @GetMapping("/category/{categoryId}")
    public ResponseEntity<?> getBooksByCategory(@PathVariable Long categoryId)
    {
        try
        {
            return ResponseEntity.ok(bookService.getBooksByCategoryResponse(categoryId));
        }
        catch (RuntimeException e)
        {
            return handleBookException(e);
        }
    }

    @GetMapping("/digital")
    public ResponseEntity<?> getDigitalBooks()
    {
        return ResponseEntity.ok(bookService.getDigitalBooksResponse());
    }

    @GetMapping("/physical")
    public ResponseEntity<?> getPhysicalBooks()
    {
        return ResponseEntity.ok(bookService.getPhysicalBooksResponse());
    }

    @PostMapping
    public ResponseEntity<?> createBook(@RequestBody BookRequest request)
    {
        try
        {
            return ResponseEntity.status(HttpStatus.CREATED).body(bookService.createBook(request));
        }
        catch (RuntimeException e)
        {
            return handleBookException(e);
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateBook(@PathVariable Long id, @RequestBody BookRequest request)
    {
        try
        {
            return ResponseEntity.ok(bookService.updateBook(id, request));
        }
        catch (RuntimeException e)
        {
            return handleBookException(e);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteBook(@PathVariable Long id)
    {
        try
        {
            bookService.deleteBook(id);
            return ResponseEntity.noContent().build();
        }
        catch (RuntimeException e)
        {
            return handleBookException(e);
        }
    }

    private ResponseEntity<String> handleBookException(RuntimeException e)
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

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(message);
    }
}
