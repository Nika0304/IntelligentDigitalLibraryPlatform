package com.library.controller;

import com.library.model.Reservation;
import com.library.service.BorrowingService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/borrowings")
public class BorrowingController
{

    private final BorrowingService borrowingService;

    public BorrowingController(BorrowingService borrowingService)
    {
        this.borrowingService = borrowingService;
    }

    @PutMapping("/{reservationId}/borrow")
    public ResponseEntity<?> borrowBook(@PathVariable Long reservationId)
    {
        try
        {
            return ResponseEntity.ok(borrowingService.borrowBook(reservationId));
        }
        catch (RuntimeException e)
        {
            return handleBorrowingException(e);
        }
    }

    @PutMapping("/{reservationId}/return")
    public ResponseEntity<?> returnBook(@PathVariable Long reservationId)
    {
        try
        {
            return ResponseEntity.ok(borrowingService.returnBook(reservationId));
        }
        catch (RuntimeException e)
        {
            return handleBorrowingException(e);
        }
    }

    @GetMapping("/active")
    public ResponseEntity<List<Reservation>> getBorrowedReservations()
    {
        return ResponseEntity.ok(borrowingService.getBorrowedReservations());
    }

    @GetMapping("/returned")
    public ResponseEntity<List<Reservation>> getReturnedReservations()
    {
        return ResponseEntity.ok(borrowingService.getReturnedReservations());
    }

    private ResponseEntity<String> handleBorrowingException(RuntimeException e)
    {
        String message = e.getMessage();

        if (message != null && message.toLowerCase().contains("not found"))
        {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(message);
        }

        if (message != null && (
                message.toLowerCase().contains("only") ||
                        message.toLowerCase().contains("not ready") ||
                        message.toLowerCase().contains("not marked") ||
                        message.toLowerCase().contains("does not have")
        ))
        {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(message);
        }

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(message);
    }
}