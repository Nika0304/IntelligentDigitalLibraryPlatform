package com.library.service;

import com.library.model.Author;
import com.library.repository.AuthorRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AuthorService
{

    private final AuthorRepository authorRepository;

    public AuthorService(AuthorRepository authorRepository)
    {
        this.authorRepository = authorRepository;
    }

    public List<Author> getAllAuthors()
    {
        return authorRepository.findAll();
    }

    public Author createAuthor(Author author)
    {
        if (author == null || author.getName() == null || author.getName().trim().isEmpty())
        {
            throw new RuntimeException("Author name cannot be empty");
        }

        //verificare duplicate
        authorRepository.findByName(author.getName().trim())
                .ifPresent(a -> {throw new RuntimeException("Author already exists");});

        return authorRepository.save(author);
    }

    public void deleteAuthor(Long id) {
        if (id == null || id <= 0)
        {
            throw new RuntimeException("Invalid author id");
        }

        if (!authorRepository.existsById(id))
        {
            throw new RuntimeException("Author not found");
        }

        authorRepository.deleteById(id);
    }
}