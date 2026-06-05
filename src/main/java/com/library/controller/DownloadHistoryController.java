package com.library.controller;

import com.library.dto.DownloadHistoryResponse;
import com.library.service.DownloadHistoryService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/downloads")
public class DownloadHistoryController
{
    private final DownloadHistoryService downloadHistoryService;

    public DownloadHistoryController(DownloadHistoryService downloadHistoryService)
    {
        this.downloadHistoryService = downloadHistoryService;
    }

    @GetMapping
    public ResponseEntity<?> getAllDownloads()
    {
        try
        {
            List<DownloadHistoryResponse> downloads = downloadHistoryService.getAllDownloads();
            return ResponseEntity.ok(downloads);
        }
        catch (Exception e)
        {
            return handleGenericException(e);
        }
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<?> getUserDownloads(@PathVariable Long userId)
    {
        try
        {
            List<DownloadHistoryResponse> downloads = downloadHistoryService.getUserDownloads(userId);
            return ResponseEntity.ok(downloads);
        }
        catch (IllegalArgumentException e)
        {
            return handleBadRequestException(e);
        }
        catch (RuntimeException e)
        {
            return handleNotFoundException(e);
        }
        catch (Exception e)
        {
            return handleGenericException(e);
        }
    }

    @GetMapping("/book/{bookId}")
    public ResponseEntity<?> getBookDownloads(@PathVariable Long bookId)
    {
        try
        {
            List<DownloadHistoryResponse> downloads = downloadHistoryService.getBookDownloads(bookId);
            return ResponseEntity.ok(downloads);
        }
        catch (IllegalArgumentException e)
        {
            return handleBadRequestException(e);
        }
        catch (RuntimeException e)
        {
            return handleNotFoundException(e);
        }
        catch (Exception e)
        {
            return handleGenericException(e);
        }
    }

    private ResponseEntity<String> handleBadRequestException(Exception e)
    {
        return ResponseEntity.badRequest().body(e.getMessage());
    }

    private ResponseEntity<String> handleNotFoundException(Exception e)
    {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
    }

    private ResponseEntity<String> handleGenericException(Exception e)
    {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Unexpected error: " + e.getMessage());
    }
}