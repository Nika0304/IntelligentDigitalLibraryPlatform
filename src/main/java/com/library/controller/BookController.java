package com.library.controller;

import com.library.dto.BookRequest;
import com.library.dto.BookResponse;
import com.library.service.BookService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.library.model.Book;
import com.library.service.PdfGeneratorService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import com.library.model.User;
import com.library.repository.UserRepository;
import com.library.service.DownloadHistoryService;
import com.library.model.DownloadHistory;
import org.springframework.security.core.context.SecurityContextHolder;
import com.library.dto.BookSearchResponse;
import com.library.service.BookSearchService;

import java.util.List;

@RestController
@RequestMapping("/api/books")
public class BookController
{
    private final BookService bookService;
    private final PdfGeneratorService pdfGeneratorService;
    private final DownloadHistoryService downloadHistoryService;
    private final UserRepository userRepository;
    private final BookSearchService bookSearchService;

    public BookController(BookService bookService,
                          PdfGeneratorService pdfGeneratorService,
                          DownloadHistoryService downloadHistoryService,
                          UserRepository userRepository,
                          BookSearchService bookSearchService)
    {
        this.bookService = bookService;
        this.pdfGeneratorService = pdfGeneratorService;
        this.downloadHistoryService = downloadHistoryService;
        this.userRepository = userRepository;
        this.bookSearchService = bookSearchService;
    }

    @GetMapping("/{id}/download")
    public ResponseEntity<?> downloadBookPdf(@PathVariable Long id)
    {
        try
        {
            Book book = bookService.getBookById(id);

            if (!book.isHasDigitalCopy())
            {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("Cartea nu are versiune digitală disponibilă.");
            }

            String email = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            User currentUser = userRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            DownloadHistory record = new DownloadHistory(currentUser, book);
            downloadHistoryService.saveDownload(record);

            byte[] pdfBytes = pdfGeneratorService.generateBookPdf(book);

            String safeTitle = book.getTitle() == null ? "book" : book.getTitle();
            String filename = URLEncoder.encode(safeTitle, StandardCharsets.UTF_8)
                    .replace("+", "%20") + ".pdf";

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.add(HttpHeaders.CONTENT_DISPOSITION,
                    "attachment; filename*=UTF-8''" + filename);
            headers.setContentLength(pdfBytes.length);

            return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);
        }
        catch (RuntimeException e)
        {
            return handleBookException(e);
        }
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


    @GetMapping("/filter")
    public ResponseEntity<?> filterBooks(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) List<Long> categoryIds,
            @RequestParam(required = false) List<Long> authorIds,
            @RequestParam(required = false, defaultValue = "all") String type,
            @RequestParam(required = false) Integer yearFrom,
            @RequestParam(required = false) Integer yearTo,
            @RequestParam(required = false) Double minRating,
            @RequestParam(required = false) Boolean onlyAvailable,
            @RequestParam(required = false, defaultValue = "relevance") String sort,
            @RequestParam(required = false, defaultValue = "0") int page,
            @RequestParam(required = false, defaultValue = "20") int size)
    {
        try
        {
            BookSearchResponse result = bookSearchService.search(
                    q, categoryIds, authorIds, type,
                    yearFrom, yearTo, minRating, onlyAvailable,
                    sort, page, size);
            return ResponseEntity.ok(result);
        }
        catch (RuntimeException e)
        {
            return handleBookException(e);
        }
    }

}
