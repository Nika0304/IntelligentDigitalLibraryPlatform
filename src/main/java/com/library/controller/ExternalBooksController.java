package com.library.controller;

import com.library.service.ExternalBooksService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/external")
public class ExternalBooksController
{
    private final ExternalBooksService externalBooksService;

    public ExternalBooksController(ExternalBooksService externalBooksService)
    {
        this.externalBooksService = externalBooksService;
    }

    @GetMapping("/books")
    public ResponseEntity<?> search(@RequestParam String q)
    {
        if(q == null || q.trim().isEmpty())
        {
            return ResponseEntity.badRequest().body("Parametrul q este obligatoriu");
        }

        return ResponseEntity.ok(externalBooksService.searchBooks(q));
    }
}