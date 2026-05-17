package com.library.controller;

import com.library.service.StatsService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/stats")
public class StatsController
{
    private final StatsService statsService;

    public StatsController(StatsService statsService)
    {
        this.statsService = statsService;
    }

    @GetMapping
    public ResponseEntity<?> getStats()
    {
        try
        {
            Map<String, Object> stats = statsService.getGeneralStats();

            return ResponseEntity.ok(stats);
        }
        catch (Exception e)
        {
            return handleGenericException(e);
        }
    }

    private ResponseEntity<String> handleGenericException(Exception e)
    {
        return ResponseEntity.status(500).body("Unexpected error: " + e.getMessage());
    }
}