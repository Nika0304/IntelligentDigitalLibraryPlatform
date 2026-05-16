package com.library.controller;

import com.library.dto.DownloadRequest;
import com.library.model.DownloadHistory;
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

    @GetMapping("/user/{userId}")
    public ResponseEntity<?> getUserDownloads(@PathVariable Long userId)
    {
        try
        {
            List<DownloadHistory> downloads = downloadHistoryService.getUserDownloads(userId);
            return ResponseEntity.ok(downloads);
        }
        catch (RuntimeException e)
        {
            return handleException(e);
        }
    }

    @PostMapping
    public ResponseEntity<?> recordDownload(@RequestBody DownloadRequest request)
    {
        try
        {
            DownloadHistory created = downloadHistoryService.recordDownload(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(created);
        }
        catch (RuntimeException e)
        {
            return handleException(e);
        }
    }

    private ResponseEntity<String> handleException(RuntimeException e)
    {
        String message = e.getMessage();

        if (message != null && message.toLowerCase().contains("not found"))
        {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(message);
        }

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(message);
    }
}
