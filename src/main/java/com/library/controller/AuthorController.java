package com.library.controller;

import com.library.model.Author;
import com.library.service.AuthorService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/authors")
public class AuthorController
{
    private final AuthorService authorService;

    public AuthorController(AuthorService authorService)
    {
        this.authorService = authorService;
    }

    // get all
    @GetMapping
    public ResponseEntity<List<Author>> getAllAuthors()
    {
        return ResponseEntity.ok(authorService.getAllAuthors());
    }

    // get by id
    @GetMapping("/{id}")
    public ResponseEntity<?> getAuthorById(@PathVariable Long id)
    {
        try
        {
            return ResponseEntity.ok(authorService.getAuthorById(id));
        }
        catch (RuntimeException e)
        {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    // create
    @PostMapping
    public ResponseEntity<?> createAuthor(@RequestBody Author author)
    {
        try
        {
            return ResponseEntity.status(HttpStatus.CREATED).body(authorService.createAuthor(author));
        }
        catch (RuntimeException e)
        {
            return handleAuthorException(e);
        }
    }

    // update
    @PutMapping("/{id}")
    public ResponseEntity<?> updateAuthor(@PathVariable Long id, @RequestBody Author author)
    {
        try
        {
            return ResponseEntity.ok(authorService.updateAuthor(id, author));
        }
        catch (RuntimeException e)
        {
            return handleAuthorException(e);
        }
    }

    // delete
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteAuthor(@PathVariable Long id)
    {
        try
        {
            authorService.deleteAuthor(id);
            return ResponseEntity.noContent().build();
        }
        catch (RuntimeException e)
        {
            return handleAuthorException(e);
        }
    }

    private ResponseEntity<String> handleAuthorException(RuntimeException e)
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