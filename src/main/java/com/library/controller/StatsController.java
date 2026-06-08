package com.library.controller;

import com.library.service.DashboardService;
import com.library.service.StatsService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/stats")
public class StatsController
{
    private final StatsService statsService;
    private final DashboardService dashboardService;

    public StatsController(StatsService statsService, DashboardService dashboardService)
    {
        this.statsService = statsService;
        this.dashboardService = dashboardService;
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
    @GetMapping("/dashboard")
    public ResponseEntity<?> dashboard() {
        return ResponseEntity.ok(dashboardService.getDashboard());
    }

    private ResponseEntity<String> handleGenericException(Exception e)
    {
        return ResponseEntity.status(500).body("Unexpected error: " + e.getMessage());
    }
}