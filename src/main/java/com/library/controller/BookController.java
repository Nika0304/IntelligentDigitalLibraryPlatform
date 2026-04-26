package com.library.controller;

import com.library.dto.BookRequest;
import com.library.model.Book;
import com.library.service.BookService;
import org.springframework.web.bind.annotation.*;
import com.library.dto.BookResponse;

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

    //get all
    @GetMapping
    public List<BookResponse> getAllBooks() {
        return bookService.getAllBooksResponse();
    }

    //get by id
    @GetMapping("/{id}")
    public Book getBookById(@PathVariable Long id)
    {
        return bookService.getBookById(id);
    }

    //cauta by title
    @GetMapping("/search")
    public List<Book> searchBooks(@RequestParam String title)
    {
        return bookService.searchBooksByTitle(title);
    }

    //creaza
    @PostMapping
    public Book createBook(@RequestBody BookRequest request)
    {
        return bookService.createBook(request);
    }

    //update
    @PutMapping("/{id}")
    public Book updateBook(@PathVariable Long id, @RequestBody BookRequest request)
    {
        return bookService.updateBook(id, request);
    }

    //delete
    @DeleteMapping("/{id}")
    public void deleteBook(@PathVariable Long id)
    {
        bookService.deleteBook(id);
    }
}