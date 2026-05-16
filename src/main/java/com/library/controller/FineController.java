package com.library.controller;

import com.library.model.Fine;
import com.library.model.FineStatus;
import com.library.service.FineService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/fines")
public class FineController
{
    private final FineService fineService;

    public FineController(FineService fineService)
    {
        this.fineService = fineService;
    }

    @GetMapping
    public ResponseEntity<?> getAllFines()
    {
        try
        {
            List<Fine> fines = fineService.getAllFines();

            return ResponseEntity.ok(fines);
        }
        catch (Exception e)
        {
            return handleGenericException(e);
        }
    }

    @GetMapping("/{fineId}")
    public ResponseEntity<?> getFineById(@PathVariable Long fineId)
    {
        try
        {
            Fine fine = fineService.getFineById(fineId);

            return ResponseEntity.ok(fine);
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

    @GetMapping("/user/{userId}")
    public ResponseEntity<?> getFinesByUserId(@PathVariable Long userId)
    {
        try
        {
            List<Fine> fines = fineService.getFinesByUserId(userId);

            return ResponseEntity.ok(fines);
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

    @GetMapping("/user/{userId}/status/{status}")
    public ResponseEntity<?> getFinesByUserIdAndStatus(@PathVariable Long userId,
                                                       @PathVariable FineStatus status)
    {
        try
        {
            List<Fine> fines = fineService.getFinesByUserIdAndStatus(userId, status);

            return ResponseEntity.ok(fines);
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

    @GetMapping("/status/{status}")
    public ResponseEntity<?> getFinesByStatus(@PathVariable FineStatus status)
    {
        try
        {
            List<Fine> fines = fineService.getFinesByStatus(status);

            return ResponseEntity.ok(fines);
        }
        catch (IllegalArgumentException e)
        {
            return handleBadRequestException(e);
        }
        catch (Exception e)
        {
            return handleGenericException(e);
        }
    }

    @PutMapping("/{fineId}/pay")
    public ResponseEntity<?> markFineAsPaid(@PathVariable Long fineId)
    {
        try
        {
            Fine fine = fineService.markFineAsPaid(fineId);

            return ResponseEntity.ok(fine);
        }
        catch (IllegalArgumentException e)
        {
            return handleBadRequestException(e);
        }
        catch (IllegalStateException e)
        {
            return handleConflictException(e);
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

    @PutMapping("/{fineId}/cancel")
    public ResponseEntity<?> cancelFine(@PathVariable Long fineId)
    {
        try
        {
            Fine fine = fineService.cancelFine(fineId);

            return ResponseEntity.ok(fine);
        }
        catch (IllegalArgumentException e)
        {
            return handleBadRequestException(e);
        }
        catch (IllegalStateException e)
        {
            return handleConflictException(e);
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

    @DeleteMapping("/{fineId}")
    public ResponseEntity<?> deleteFine(@PathVariable Long fineId)
    {
        try
        {
            fineService.deleteFine(fineId);

            return ResponseEntity.noContent().build();
        }
        catch (IllegalArgumentException e)
        {
            return handleBadRequestException(e);
        }
        catch (IllegalStateException e)
        {
            return handleConflictException(e);
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
        return ResponseEntity.status(404).body(e.getMessage());
    }

    private ResponseEntity<String> handleConflictException(Exception e)
    {
        return ResponseEntity.status(409).body(e.getMessage());
    }

    private ResponseEntity<String> handleGenericException(Exception e)
    {
        return ResponseEntity.status(500).body("Unexpected error: " + e.getMessage());
    }
}