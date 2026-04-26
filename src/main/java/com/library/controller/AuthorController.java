package com.library.controller;

import com.library.model.Author;
import com.library.service.AuthorService;
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

    //get all
    @GetMapping
    public List<Author> getAllAuthors()
    {
        return authorService.getAllAuthors();
    }

    //create
    @PostMapping
    public Author createAuthor(@RequestBody Author author)
    {
        return authorService.createAuthor(author);
    }

    // DELETE
    @DeleteMapping("/{id}")
    public void deleteAuthor(@PathVariable Long id)
    {
        authorService.deleteAuthor(id);
    }
}