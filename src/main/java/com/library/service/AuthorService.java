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

    public Author getAuthorById(Long id)
    {
        return authorRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Author not found with id: " + id));
    }

    public Author createAuthor(Author author)
    {
        if (author.getName() == null || author.getName().trim().isEmpty())
        {
            throw new RuntimeException("Author name is required");
        }

        if (authorRepository.findByName(author.getName()).isPresent())
        {
            throw new RuntimeException("Author already exists: " + author.getName());
        }

        return authorRepository.save(author);
    }

    public Author updateAuthor(Long id, Author request)
    {
        Author author = authorRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Author not found with id: " + id));

        if (request.getName() == null || request.getName().trim().isEmpty())
        {
            throw new RuntimeException("Author name is required");
        }

        authorRepository.findByName(request.getName()).ifPresent(existingAuthor -> {
            if (!existingAuthor.getAuthorId().equals(id))
            {
                throw new RuntimeException("Author already exists: " + request.getName());
            }
        });

        author.setName(request.getName());

        return authorRepository.save(author);
    }

    public void deleteAuthor(Long id)
    {
        Author author = authorRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Author not found with id: " + id));

        authorRepository.delete(author);
    }
}